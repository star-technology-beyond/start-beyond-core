package com.startechnology.start_core.machine.cross_dim_laser;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableLaserContainer;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.LaserHatchPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.startechnology.start_core.mixin.LaserHatchPartMachineAccessor;

import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class StarTCrossDimensionalLaserMachine extends WorkableMultiblockMachine
        implements IFancyUIMachine, IDisplayUIMachine {

    public enum CrossDimensionalLaserDirection {
        SENDER,
        RECEIVER
    }

    public enum LinkedStatus {
        Linked,
        Unlinked
    }

    @Persisted
    @DescSynced
    @Getter
    private CrossDimensionalLaserDirection direction;

    @Persisted
    private Long linkKey;

    @Getter
    private Optional<GlobalPos> partnerGlobalPos = Optional.empty();

    @DescSynced
    @Getter
    private LinkedStatus linkStatus = LinkedStatus.Unlinked;

    private NotifiableLaserContainer laserContainer = null;
    private ISubscription laserSubscription = null;

    private NotifiableItemStackHandler inputInventory = null;
    private TickableSubscription tryCrossDimTickSub = null;
    private ISubscription inputSubscription = null;

    protected boolean readyToUpdate = false;

    public StarTCrossDimensionalLaserMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (laserSubscription != null) laserSubscription.unsubscribe();
        if (inputSubscription != null) inputSubscription.unsubscribe();
        if (tryCrossDimTickSub != null) tryCrossDimTickSub.unsubscribe();

        readyToUpdate = false;

        /*
         * Get the first laser hatch for input/output and the
         * input busses inventory for the singularity
         * The multiblock structure enforces that this is not null since
         * we are guaranteed one of the parts
         * 
         * err kaboom or something if that invariant fails
         */
        laserContainer = null;
        inputInventory = null;

        for (IMultiPart part : getParts()) {
            if (part instanceof LaserHatchPartMachine laserHatch) {
                laserContainer = ((LaserHatchPartMachineAccessor) laserHatch).start_core$getLaserContainer();
                laserSubscription = laserContainer.addChangedListener(this::transferEnergyTick);
            } else if (part instanceof ItemBusPartMachine inputBus) {
                inputInventory = inputBus.getInventory();
                inputSubscription = inputInventory.addChangedListener(this::tryCrossDimTick);
            }
        }

        if (laserContainer.getHandlerIO().support(IO.IN)) {
            direction = CrossDimensionalLaserDirection.SENDER;
        } else {
            direction = CrossDimensionalLaserDirection.RECEIVER;
        }

        readyToUpdate = true;
        tryCrossDimTickSub = subscribeServerTick(tryCrossDimTickSub,
                this::tryCrossDimTick);
        discoverSingularityOrReset();
    }

    protected void tryCrossDimTick() {
        if (isRemote())
            return;
        if (getOffsetTimer() % 60 == 0) {
            discoverSingularityOrReset();
        }
        transferEnergyTick();
    }

    protected void transferEnergyTick() {
        if (isRemote())
            return;
        if (!readyToUpdate || !isWorkingEnabled() || laserContainer == null
                || linkStatus != LinkedStatus.Linked || linkKey == null) {
            return;
        }

        // Only the SENDER pushes energy.
        if (direction != CrossDimensionalLaserDirection.SENDER)
            return;

        long available = laserContainer.getEnergyStored();
        if (available <= 0)
            return;

        Level level = getLevel();
        CrossDimensionalLaserSavedData savedData = CrossDimensionalLaserSavedData.get(level);
        if (savedData == null)
            return;

        savedData.getReceiver(linkKey).ifPresent(receiverPos -> {
            // Yoink the block entity of the reciever
            ServerLevel receiverLevel = level.getServer().getLevel(receiverPos.dimension());
            if (receiverLevel == null)
                return;

            if (!(receiverLevel.getBlockEntity(
                    receiverPos.pos()) instanceof com.gregtechceu.gtceu.api.machine.IMachineBlockEntity receiverBE)) {
                return;
            }

            // Make sure its another StarTCrossDimensionalLaserMachine
            if (!(receiverBE.getMetaMachine() instanceof StarTCrossDimensionalLaserMachine receiverMachine)) {
                return;
            }

            NotifiableLaserContainer receiverLaser = receiverMachine.laserContainer;
            if (receiverLaser == null)
                return;

            // Limit by the reciever hatch amps
            long toTransfer = Math.min(available, receiverLaser.getOutputVoltage() * receiverLaser.getOutputAmperage());

            // lasery laser laser :3
            long transferredAmount = receiverLaser.changeEnergy(toTransfer);
            laserContainer.changeEnergy(-transferredAmount);
        });
    }

    protected void discoverSingularityOrReset() {
        boolean wasRegistered = !Objects.isNull(linkKey);

        tryDiscoverSingularity();

        if (wasRegistered && Objects.isNull(linkKey)) {
            unRegisterPair();
        }

        // Refresh link status from saved data after any change.
        refreshLinkStatus();
    }

    protected void tryDiscoverSingularity() {
        linkKey = null;

        if (isRemote())
            return;
        if (!readyToUpdate || !isWorkingEnabled() || inputInventory == null)
            return;

        // Try read the AE2 singularity in slot 0 of the bus
        ItemStack stack = inputInventory.getStackInSlot(0);
        if (!QuantumBridgeBlockEntity.isValidEntangledSingularity(stack))
            return;

        // Update the linking if we get a new link key
        long newLinkKey = stack.getOrCreateTag().getLong(QuantumBridgeBlockEntity.TAG_FREQUENCY);
        Level level = getLevel();
        CrossDimensionalLaserSavedData savedData = CrossDimensionalLaserSavedData.get(level);
        if (savedData == null)
            return;

        linkKey = newLinkKey;
        registerSelf(newLinkKey, level, savedData);
    }

    protected void registerSelf(long linkId, Level level, CrossDimensionalLaserSavedData savedData) {
        GlobalPos position = GlobalPos.of(level.dimension(), getPos());

        switch (direction) {
            case SENDER -> {
                savedData.registerSender(linkId, position);
            }
            case RECEIVER -> {
                savedData.registerReceiver(linkId, position);
            }
        }
    }

    protected void refreshLinkStatus() {
        if (isRemote())
            return;

        if (linkKey == null) {
            linkStatus = LinkedStatus.Unlinked;
            return;
        }

        Level level = getLevel();
        CrossDimensionalLaserSavedData savedData = CrossDimensionalLaserSavedData.get(level);
        if (savedData == null) {
            linkStatus = LinkedStatus.Unlinked;
            return;
        }

        Optional<GlobalPos> sender = savedData.getSender(linkKey);
        Optional<GlobalPos> receiver = savedData.getReceiver(linkKey);

        linkStatus = (sender.isPresent() && receiver.isPresent())
                ? LinkedStatus.Linked
                : LinkedStatus.Unlinked;

        var partnerOpt = direction == CrossDimensionalLaserDirection.SENDER
                ? savedData.getReceiver(linkKey)
                : savedData.getSender(linkKey);

        partnerGlobalPos = Optional.empty();
        partnerOpt.ifPresent(partner -> {
            partnerGlobalPos = Optional.of(partner);
        });
    }

    protected void unRegisterPair() {
        Level level = getLevel();
        GlobalPos position = GlobalPos.of(level.dimension(), getPos());
        CrossDimensionalLaserSavedData savedData = CrossDimensionalLaserSavedData.get(level);
        if (savedData != null) {
            savedData.unregister(position);
        }
        linkStatus = LinkedStatus.Unlinked;
    }

    @Override
    public void onStructureInvalid() {
        if (!isRemote() && linkKey != null) {
            unRegisterPair();
            linkKey = null;
        }

        super.onStructureInvalid();
        this.readyToUpdate = false;
        this.laserContainer = null;
        this.inputInventory = null;

        if (!Objects.isNull(laserSubscription)) {
            laserSubscription.unsubscribe();
        }

        if (!Objects.isNull(tryCrossDimTickSub)) {
            tryCrossDimTickSub.unsubscribe();
        }

        if (!Objects.isNull(inputSubscription)) {
            inputSubscription.unsubscribe();
        }
    }

    public void addLinkDisplayText(List<Component> textList) {
        if (!isFormed()) {
            textList.add(Component.translatable("gtceu.multiblock.invalid_structure")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        textList.add(Component.empty());

        textList.add(Component.translatable("ui.start_core.cross_dimensional_laser.type"));
        textList.add(Component.translatable(direction == CrossDimensionalLaserDirection.SENDER
                ? "ui.start_core.cross_dimensional_laser.sender"
                : "ui.start_core.cross_dimensional_laser.receiver"));

        textList.add(Component.empty());
        textList.add(Component.translatable("ui.start_core.cross_dimensional_laser.link_status"));

        switch (linkStatus) {
            case Linked -> {
                textList.add(Component.translatable("ui.start_core.cross_dimensional_laser.linked"));

                // Show linked coords if available
                if (linkKey != null) {
                    partnerGlobalPos.ifPresent(partner -> {
                        textList.add(
                                Component.translatable(
                                        "ui.start_core.cross_dimensional_laser.linked_location_dim",
                                        partner.dimension().location()));

                        textList.add(
                                Component.translatable(
                                        "ui.start_core.cross_dimensional_laser.linked_location_coords",
                                        partner.pos().getX(),
                                        partner.pos().getY(),
                                        partner.pos().getZ()));
                    });
                }
                ;

                if (direction == CrossDimensionalLaserDirection.SENDER) {
                    MutableComponent sentAmountComponent = Component
                            .literal(FormattingUtil.formatNumbers(this.laserContainer.getOutputPerSec() / 20))
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                    textList.add(Component
                            .translatable("ui.start_core.cross_dimensional_laser.output_per_sec", sentAmountComponent)
                            .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable(
                                            "ui.start_core.cross_dimensional_laser.hatch.output_per_sec_hover")))));
                } else {
                    MutableComponent recvAmountComponent = Component
                            .literal(FormattingUtil.formatNumbers(this.laserContainer.getInputPerSec() / 20))
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN));
                    textList.add(Component
                            .translatable("ui.start_core.cross_dimensional_laser.input_per_sec", recvAmountComponent)
                            .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable(
                                            "ui.start_core.cross_dimensional_laser.hatch.input_per_sec_hover")))));
                }
            }
            case Unlinked -> {
                textList.add(
                        Component.translatable("ui.start_core.cross_dimensional_laser.unlinked"));

                // Display helpful messsage that theres no valid singularity yet.
                if (inputInventory != null &&
                    !QuantumBridgeBlockEntity.isValidEntangledSingularity(inputInventory.getStackInSlot(0))) {
                    textList.add(
                        Component.translatable("ui.start_core.cross_dimensional_laser.no_valid_item"));
                }

            }
        }
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        group.addWidget(
                new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(GuiTextures.DISPLAY)
                        .addWidget(new LabelWidget(4, 5, "Cross-Dimensional Laser Tunneling Array"))
                        .addWidget(new ComponentPanelWidget(4, 15, this::addLinkDisplayText)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return true;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer)
                .widget(new FancyMachineUIWidget(this, 198, 208));
    }
}