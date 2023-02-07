package com.github.zly2006.clienttweaks.mixin;

import com.github.zly2006.clienttweaks.VisibleChatHudLineAccess;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    private static final Pattern urlPattern = Pattern.compile("(https?://)?[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,3}(/\\S*)?");

    private Text currentMessage;

    @Inject(
        method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
        at = @At("HEAD")
    )
    private void ct$addMessage(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
        currentMessage = message;
    }

    @ModifyArg(
        method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
        at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", ordinal = 0),
        index = 1
    )
    private Object ct$addVisibleMessage(Object element) {
        ChatHudLine.Visible visible = (ChatHudLine.Visible) element;
        ((VisibleChatHudLineAccess) element).setText(currentMessage);
        currentMessage = null;
        return visible;
    }
}
