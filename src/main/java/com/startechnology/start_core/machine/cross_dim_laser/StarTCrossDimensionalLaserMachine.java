package com.startechnology.start_core.machine.cross_dim_laser;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableLaserContainer;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.LaserHatchPartMachine;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.startechnology.start_core.mixin.LaserHatchPartMachineAccessor;

import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.core.definitions.AEItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
        Unlinked,
        LinkedToWrongType
    }

    @Persisted
    @DescSynced
    private CrossDimensionalLaserDirection direction;

    @Persisted
    private Long linkKey;

    @DescSynced
    private LinkedStatus linkStatus = LinkedStatus.Unlinked;

    private NotifiableLaserContainer laserContainer = null;
    private NotifiableItemStackHandler inputInventory = null;
    protected boolean readyToUpdate = false;

    public StarTCrossDimensionalLaserMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

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
                laserContainer.addChangedListener(this::transferEnergyTick);
                break;
            }

            if (part instanceof ItemBusPartMachine inputBus) {
                inputInventory = inputBus.getInventory();
            }
        }

        if (laserContainer.getHandlerIO().support(IO.IN)) {
            direction = StarTCrossDimensionalLaserMachine.CrossDimensionalLaserDirection.SENDER;
        } else {
            direction = StarTCrossDimensionalLaserMachine.CrossDimensionalLaserDirection.RECEIVER;
        }

        readyToUpdate = true;
        discoverSingularityOrReset();
    }

    protected void transferEnergyTick() {
        if (isRemote())
            return;
        if (!readyToUpdate || !isWorkingEnabled() || laserContainer == null || linkStatus != LinkedStatus.Linked) {
            return;
        }
    }

    protected void discoverSingularityOrReset() {
        boolean wasRegistered = !Objects.isNull(linkKey);

        tryDiscoverSingularity();

        if (wasRegistered && Objects.isNull(linkKey)) {
            unRegisterPair();
        }
    }

    protected void tryDiscoverSingularity() {
        linkKey = null;

        if (isRemote())
            return;
        if (!readyToUpdate || !isWorkingEnabled() || inputInventory == null) {
            return;
        }

        ItemStack stack = inputInventory.getStackInSlot(0);
        if (!QuantumBridgeBlockEntity.isValidEntangledSingularity(stack)) {
            return;
        }

        // Get the AE2 frequency from the stack as our link key
        long newLinkKey = stack.getOrCreateTag().getLong(QuantumBridgeBlockEntity.TAG_FREQUENCY); 
        Level level = getLevel();
        CrossDimensionalLaserSavedData savedData = CrossDimensionalLaserSavedData.get(level)

        // Update the link key if it changed, or theres no pair for this 
        if (newLinkKey != linkKey || !savedData.hasPair(newLinkKey)) {
            linkKey = newLinkKey;
            registerSelf(newLinkKey, level, savedData);
        }
    }

    protected void registerSelf(long linkId, Level level, CrossDimensionalLaserSavedData savedData) {
        GlobalPos position = GlobalPos.of(level.dimension(), getPos());

        

        switch (direction) {
            case RECEIVER:
                savedData.registerReceiver(linkKey, position);
                break;
            case SENDER:
                savedData.registerSender(linkKey, position);
                break;
            default:
                break;
        }
    }

    protected void unRegisterPair() {
        Level level = getLevel();
        GlobalPos position = GlobalPos.of(level.dimension(), getPos());
        CrossDimensionalLaserSavedData savedData = CrossDimensionalLaserSavedData.get(level);
        savedData.unregister(position);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.readyToUpdate = false;
        this.laserContainer = null;
    }

    public void addLinkDisplayText(List<Component> textList) {
        if (!isFormed()) {
            textList.add(Component.translatable("gtceu.multiblock.invalid_structure")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        textList.add(Component.empty());

        /* Display for the current direction of the laser array */
        textList.add(Component.translatable("ui.start_core.cross_dimensional_laser.type"));
        textList.add(Component.translatable(direction == CrossDimensionalLaserDirection.SENDER
                ? "ui.start_core.cross_dimensional_laser.sender"
                : "ui.start_core.cross_dimensional_laser.receiver"));

        /* Display of the current linked status of the multiblock */
        textList.add(Component.empty());
        textList.add(Component.translatable("ui.start_core.cross_dimensional_laser.link_status"));
        switch (linkStatus) {
            case Linked:
                textList.add(Component.translatable("ui.start_core.cross_dimensional_laser.linked"));
                textList.add(Component.translatable("ui.start_core.cross_dimensional_laser.linked_location", "meow"));
                break;
            case LinkedToWrongType:
                textList.add(Component.translatable("ui.start_core.cross_dimensional_laser.linked_to_wrong_type"));
                break;
            case Unlinked:
                textList.add(Component.translatable("ui.start_core.cross_dimensional_laser.unlinked"));
                break;
            default:
                break;

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
        ModularUI ui = new ModularUI(198, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 198, 208));
        return ui;
    }
}
