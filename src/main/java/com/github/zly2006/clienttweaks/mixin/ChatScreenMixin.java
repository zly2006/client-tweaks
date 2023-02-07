package com.github.zly2006.clienttweaks.mixin;

import com.github.zly2006.clienttweaks.VisibleChatHudLineAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    boolean justClickedMenu = false;
    List<ButtonWidget> addedElements;
    private static final Pattern urlPattern = Pattern.compile("(https?://)?[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,3}(/\\S*)?");

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Shadow @Nullable protected abstract Style getTextStyleAt(double x, double y);

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private void ct$keyPressed(MinecraftClient client, Screen screen) {
        if (screen == null) {
            if (client.currentScreen == ct$getThis()) {
                client.setScreen(null);
            }
        } else {
            client.setScreen(screen);
        }
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void ct$removeAddedButtons(CallbackInfoReturnable<Boolean> cir) {
        if (!justClickedMenu) {
            if (addedElements != null) {
                clearAddedElements();
            }
        }
        else {
            justClickedMenu = false;
        }
    }

    private void clearAddedElements() {
        addedElements.forEach(this::remove);
        addedElements = null;
        justClickedMenu = false;
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void ct$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_2) { // Right click
            System.out.println("Right click");
            MinecraftClient client = MinecraftClient.getInstance();
            ChatHudLine.Visible visible = ct$geMessageAt(mouseX, mouseY);
            if (visible != null) {
                rightClickMenu((int) mouseX, (int) mouseY, client, visible);
                cir.setReturnValue(true);
            }
        }
    }

    private void rightClickMenu(int mouseX, int mouseY, MinecraftClient client, ChatHudLine.Visible visible) {
        if (addedElements != null) {
            clearAddedElements();
        }
        addedElements = new ArrayList<>();
        OrderedText text = visible.content();
        StringBuilder builder = new StringBuilder();
        text.accept((index, style, codePoint) -> {
            builder.append(Character.toChars(codePoint));
            return true;
        });
        String message = builder.toString();
        Matcher matcher = urlPattern.matcher(message);
        if (matcher.find()) {
            String url = matcher.group();
            if (!url.startsWith("http")) {
                url = "http://" + url;
            }
            String finalUrl = url;
            ButtonWidget urlButton = ButtonWidget.builder(Text.literal("Copy URL"), buttonWidget -> {
                    client.keyboard.setClipboard(finalUrl);
                    buttonWidget.active = false;
                    buttonWidget.setMessage(Text.literal("Copied"));
                    justClickedMenu = true;
                })
                .size(50, 20)
                .build();
            addedElements.add(urlButton);
        }
        if (!message.isEmpty()) {
            ButtonWidget copyButton = ButtonWidget.builder(Text.literal("Copy"), buttonWidget -> {
                    client.keyboard.setClipboard(message);
                    buttonWidget.active = false;
                    buttonWidget.setMessage(Text.literal("Copied"));
                    justClickedMenu = true;
                })
                .size(50, 20)
                .build();
            addedElements.add(copyButton);
        }
        if (visible instanceof VisibleChatHudLineAccess access && access.getText() != null) {
            ButtonWidget translateButton = ButtonWidget.builder(Text.literal("Copy Raw"), buttonWidget -> {
                    client.keyboard.setClipboard(Text.Serializer.toJson(access.getText()));
                    buttonWidget.active = false;
                    buttonWidget.setMessage(Text.literal("Copied"));
                    justClickedMenu = true;
                })
                .size(50, 20)
                .build();
            addedElements.add(translateButton);
        }
        // Add them to the screen
        int y;
        if (height - mouseY > 20 * addedElements.size()) {
            y = mouseY;
        }
        else {
            y = mouseY - 20 * addedElements.size();
        }
        for (ButtonWidget element : addedElements) {
            addDrawableChild(element);
            element.setX(mouseX);
            element.setY(y);
            y += 20;
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"))
    private void ct$mouseScrolled(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
        if (amount != 0) {
            if (addedElements != null) {
                for (Element element : addedElements) {
                    remove(element);
                }
                addedElements = null;
            }
        }
    }

    private ChatHudLine.Visible ct$geMessageAt(double x, double y) {
        ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
        double d = chatHud.toChatLineX(x);
        int i = chatHud.getMessageLineIndex(d, chatHud.toChatLineY(y));
        if (i >= 0 && i < chatHud.visibleMessages.size()) {
            return chatHud.visibleMessages.get(i);
        }
        return null;
    }

    private ChatScreen ct$getThis() {
        return (ChatScreen) (Object) this;
    }
}
