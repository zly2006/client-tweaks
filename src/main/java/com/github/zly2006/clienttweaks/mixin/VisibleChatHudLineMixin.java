package com.github.zly2006.clienttweaks.mixin;

import com.github.zly2006.clienttweaks.VisibleChatHudLineAccess;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatHudLine.Visible.class)
public class VisibleChatHudLineMixin implements VisibleChatHudLineAccess {
    private Text text;
    @Override
    public Text getText() {
        return this.text;
    }

    @Override
    public void setText(Text text) {
        this.text = text;
    }
}
