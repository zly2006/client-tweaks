package com.github.zly2006.clienttweaks;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

public interface VisibleChatHudLineAccess {
    Text getText();
    void setText(Text text);
}
