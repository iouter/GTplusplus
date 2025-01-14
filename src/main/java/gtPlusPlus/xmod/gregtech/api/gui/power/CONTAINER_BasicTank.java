package gtPlusPlus.xmod.gregtech.api.gui.power;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.gui.GT_ContainerMetaTile_Machine;
import gregtech.api.gui.GT_Slot_Output;
import gregtech.api.gui.GT_Slot_Render;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gtPlusPlus.xmod.gregtech.api.metatileentity.custom.power.GTPP_MTE_BasicTank;
import java.util.Iterator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

public class CONTAINER_BasicTank extends GT_ContainerMetaTile_Machine {
    public int mContent = 0;

    public CONTAINER_BasicTank(InventoryPlayer aInventoryPlayer, IGregTechTileEntity aTileEntity) {
        super(aInventoryPlayer, aTileEntity);
    }

    public void addSlots(InventoryPlayer aInventoryPlayer) {
        this.addSlotToContainer(new Slot(this.mTileEntity, 0, 80, 17));
        this.addSlotToContainer(new GT_Slot_Output(this.mTileEntity, 1, 80, 53));
        this.addSlotToContainer(new GT_Slot_Render(this.mTileEntity, 2, 59, 42));
    }

    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (!this.mTileEntity.isClientSide() && this.mTileEntity.getMetaTileEntity() != null) {
            if (((GTPP_MTE_BasicTank) this.mTileEntity.getMetaTileEntity()).mFluid != null) {
                this.mContent = ((GTPP_MTE_BasicTank) this.mTileEntity.getMetaTileEntity()).mFluid.amount;
            } else {
                this.mContent = 0;
            }

            Iterator var2 = this.crafters.iterator();

            while (var2.hasNext()) {
                ICrafting var1 = (ICrafting) var2.next();
                var1.sendProgressBarUpdate(this, 100, this.mContent & '￿');
                var1.sendProgressBarUpdate(this, 101, this.mContent >>> 16);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int par1, int par2) {
        super.updateProgressBar(par1, par2);
        switch (par1) {
            case 100:
                this.mContent = this.mContent & -65536 | par2;
                break;
            case 101:
                this.mContent = this.mContent & '￿' | par2 << 16;
        }
    }

    public int getSlotCount() {
        return 2;
    }

    public int getShiftClickSlotCount() {
        return 1;
    }
}
