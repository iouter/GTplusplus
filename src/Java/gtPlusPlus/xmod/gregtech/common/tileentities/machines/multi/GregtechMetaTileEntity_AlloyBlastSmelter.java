package gtPlusPlus.xmod.gregtech.common.tileentities.machines.multi;

import gregtech.api.enums.Textures;
import gregtech.api.gui.GT_GUIContainer_MultiMachine;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.objects.GT_RenderedTexture;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gregtech.api.util.Recipe_GT;
import gtPlusPlus.core.block.ModBlocks;
import gtPlusPlus.core.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class GregtechMetaTileEntity_AlloyBlastSmelter
        extends GT_MetaTileEntity_MultiBlockBase {
    private int mHeatingCapacity = 0;

    public GregtechMetaTileEntity_AlloyBlastSmelter(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GregtechMetaTileEntity_AlloyBlastSmelter(String aName) {
        super(aName);
    }

    @Override
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GregtechMetaTileEntity_AlloyBlastSmelter(this.mName);
    }

    @Override
	public String[] getDescription() {
        return new String[]{
        		"Controller Block for the Alloy Blast Smelter", //Outputs 144mb fluid for every inputStack.stackSize; Time to use those hot metals.
        		"Size: 3x3x4 (Hollow)",
        		"Controller (front middle at bottom)",
        		"16x Blast Smelter Heat Containment Coils (two middle Layers, hollow)",
        		"1x Input bus (one of bottom)",
        		"1x Output Hatch (one of bottom)",
        		"1x Energy Hatch (one of bottom)", 
        		"1x Maintenance Hatch (one of bottom)",
        		"1x Muffler Hatch (top middle)",
        		"Blast Smelter Casings for the rest"};
    }

    @Override
	public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        if (aSide == aFacing) {
            return new ITexture[]{Textures.BlockIcons.CASING_BLOCKS[72], new GT_RenderedTexture(aActive ? Textures.BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_ACTIVE : Textures.BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE)};
        }
        return new ITexture[]{Textures.BlockIcons.CASING_BLOCKS[72]};
    }

    @Override
	public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_GUIContainer_MultiMachine(aPlayerInventory, aBaseMetaTileEntity, getLocalName(), "ElectricBlastFurnace.png");
    }

    @Override
	public GT_Recipe.GT_Recipe_Map getRecipeMap() {
        return Recipe_GT.Gregtech_Recipe_Map.sAlloyBlastSmelterRecipes;
    }

    @Override
	public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
	public boolean isFacingValid(byte aFacing) {
        return aFacing > 1;
    }

    @Override
	public boolean checkRecipe(ItemStack aStack) {
        ArrayList<ItemStack> tInputList = getStoredInputs();
        for (int i = 0; i < tInputList.size() - 1; i++) {
            for (int j = i + 1; j < tInputList.size(); j++) {
                if (GT_Utility.areStacksEqual((ItemStack) tInputList.get(i), (ItemStack) tInputList.get(j))) {
                    if (((ItemStack) tInputList.get(i)).stackSize >= ((ItemStack) tInputList.get(j)).stackSize) {
                        tInputList.remove(j--);
                    } else {
                        tInputList.remove(i--);
                        break;
                    }
                }
            }
        }
        ItemStack[] tInputs = (ItemStack[]) Arrays.copyOfRange(tInputList.toArray(new ItemStack[tInputList.size()]), 0, 2);

        ArrayList<FluidStack> tFluidList = getStoredFluids();
        for (int i = 0; i < tFluidList.size() - 1; i++) {
            for (int j = i + 1; j < tFluidList.size(); j++) {
                if (GT_Utility.areFluidsEqual((FluidStack) tFluidList.get(i), (FluidStack) tFluidList.get(j))) {
                    if (((FluidStack) tFluidList.get(i)).amount >= ((FluidStack) tFluidList.get(j)).amount) {
                        tFluidList.remove(j--);
                    } else {
                        tFluidList.remove(i--);
                        break;
                    }
                }
            }
        }
        FluidStack[] tFluids = (FluidStack[]) Arrays.copyOfRange(tFluidList.toArray(new FluidStack[tInputList.size()]), 0, 1);
        if (tInputList.size() > 0) {
        	Utils.LOG_INFO("Found some Valid Inputs.");
            long tVoltage = getMaxInputVoltage();
            byte tTier = (byte) Math.max(1, GT_Utility.getTier(tVoltage));
            GT_Recipe tRecipe = GT_Recipe.GT_Recipe_Map.sBlastRecipes.findRecipe(getBaseMetaTileEntity(), false, gregtech.api.enums.GT_Values.V[tTier], tFluids, tInputs);
            if ((tRecipe != null) && (this.mHeatingCapacity >= tRecipe.mSpecialValue) && (tRecipe.isRecipeInputEqual(true, tFluids, tInputs))) {
                this.mEfficiency = (10000 - (getIdealStatus() - getRepairStatus()) * 1000);
                this.mEfficiencyIncrease = 10000;
                if (tRecipe.mEUt <= 16) {
                    this.mEUt = (tRecipe.mEUt * (1 << tTier - 1) * (1 << tTier - 1));
                    this.mMaxProgresstime = (tRecipe.mDuration / (1 << tTier - 1));
                } else {
                    this.mEUt = tRecipe.mEUt;
                    this.mMaxProgresstime = tRecipe.mDuration;
                    while (this.mEUt <= gregtech.api.enums.GT_Values.V[(tTier - 1)]) {
                        this.mEUt *= 4;
                        this.mMaxProgresstime /= 2;
                    }
                }
                if (this.mEUt > 0) {
                    this.mEUt = (-this.mEUt);
                }
                this.mMaxProgresstime = Math.max(1, this.mMaxProgresstime);
                this.mOutputItems = new ItemStack[]{tRecipe.getOutput(0), tRecipe.getOutput(1)};
                updateSlots();
                return true;
            }
        }
    	Utils.LOG_INFO("Failed to find some Valid Inputs.");
        return false;
    }

    @Override
	public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        int xDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetX;
        int zDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetZ;

        //this.mHeatingCapacity = 0;
        if (!aBaseMetaTileEntity.getAirOffset(xDir, 1, zDir)) {
            return false;
        }
        if (!aBaseMetaTileEntity.getAirOffset(xDir, 2, zDir)) {
            return false;
        }
        addMufflerToMachineList(aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir, 3, zDir), 72);

        byte tUsedMeta = aBaseMetaTileEntity.getMetaIDOffset(xDir + 1, 2, zDir);
             
        this.mHeatingCapacity = 20000;
        
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if ((i != 0) || (j != 0)) {
                    if (aBaseMetaTileEntity.getBlockOffset(xDir + i, 2, zDir + j) != ModBlocks.blockCasingsMisc) {
                        return false;
                    }
                    if (aBaseMetaTileEntity.getMetaIDOffset(xDir + i, 2, zDir + j) != 14) {
                        return false;
                    }
                    if (aBaseMetaTileEntity.getBlockOffset(xDir + i, 1, zDir + j) != ModBlocks.blockCasingsMisc) {
                        return false;
                    }
                    if (aBaseMetaTileEntity.getMetaIDOffset(xDir + i, 1, zDir + j) != 14) {
                        return false;
                    }
                    if (aBaseMetaTileEntity.getBlockOffset(xDir + i, 3, zDir + j) != ModBlocks.blockCasingsMisc) {
                        return false;
                    }
                    if (aBaseMetaTileEntity.getMetaIDOffset(xDir + i, 3, zDir + j) != 15) {
                        return false;
                    }
                }
            }
        }
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if ((xDir + i != 0) || (zDir + j != 0)) {
                    IGregTechTileEntity tTileEntity = aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + i, 0, zDir + j);
                    if ((!addMaintenanceToMachineList(tTileEntity, 72)) && (!addInputToMachineList(tTileEntity, 72)) && (!addOutputToMachineList(tTileEntity, 72)) && (!addEnergyInputToMachineList(tTileEntity, 72))) {
                        if (aBaseMetaTileEntity.getBlockOffset(xDir + i, 0, zDir + j) != ModBlocks.blockCasingsMisc) {
                            return false;
                        }
                        if (aBaseMetaTileEntity.getMetaIDOffset(xDir + i, 0, zDir + j) != 15) {
                            return false;
                        }
                    }
                }
            }
        }
        this.mHeatingCapacity += 100 * (GT_Utility.getTier(getMaxInputVoltage()) - 2);
        return true;
    }

    @Override
	public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    @Override
	public int getPollutionPerTick(ItemStack aStack) {
        return 10;
    }

    @Override
	public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
	public int getAmountOfOutputs() {
        return 2;
    }

    @Override
	public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }
}