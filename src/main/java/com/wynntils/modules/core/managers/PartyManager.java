/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.core.managers;

import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.containers.PartyContainer;
import com.wynntils.core.framework.instances.data.SocialData;
import com.wynntils.core.utils.helpers.CommandResponse;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;

import java.util.regex.Pattern;

public class PartyManager {

    private static final Pattern listPattern = Pattern.compile("(§eParty members:|§eYou must be in a party to list\\.)");
    private static final CommandResponse listExecutor = new CommandResponse("/party list", (matcher, text) -> {
        String entire = matcher.group(0);
        if (entire.contains("You must be in")) {  // clears the party
            PlayerInfo.get(SocialData.class).getPlayerParty().removeMember(Minecraft.getInstance().player.getName());
            return;
        }

        PartyContainer partyContainer = PlayerInfo.get(SocialData.class).getPlayerParty();
        for (ITextComponent components : text.getSiblings()) {
            if (components.getFormattedText().startsWith("§e")) continue;

            boolean owner = components.getFormattedText().startsWith("§b");
            String member = components.getUnformattedText().contains(",") ? components.getUnformattedText().split(",")[0] : components.getUnformattedText();

            if (owner) partyContainer.setOwner(member);
            partyContainer.addMember(member);
        }

    }, listPattern);

    public static void handlePartyList() {
        listExecutor.executeCommand();
    }

    public static void handleMessages(ITextComponent component) {
        if (component.getUnformattedText().startsWith("You have successfully joined the party.")) {
            handlePartyList();
            return;
        }
        if (component.getUnformattedText().startsWith("You have been removed from the party.")
                || component.getUnformattedText().startsWith("Your party has been disbanded since you were the only member remaining.")
                || component.getUnformattedText().startsWith("Your party has been disbanded.")) {
            PartyContainer partyContainer = PlayerInfo.get(SocialData.class).getPlayerParty();
            partyContainer.removeMember(Minecraft.getInstance().player.getName());
            return;
        }
        if (component.getUnformattedText().startsWith("You have successfully created a party.")) {
            PartyContainer partyContainer = PlayerInfo.get(SocialData.class).getPlayerParty();
            partyContainer.setOwner(Minecraft.getInstance().player.getName());
            partyContainer.addMember(Minecraft.getInstance().player.getName());
            return;
        }
        if (component.getFormattedText().startsWith("§e") && component.getUnformattedText().contains("has joined the party.")) {
            PartyContainer partyContainer = PlayerInfo.get(SocialData.class).getPlayerParty();

            String member = component.getUnformattedText().split(" has joined the party.")[0];
            partyContainer.addMember(member);
            return;
        }
        if (component.getFormattedText().startsWith("§e") && component.getUnformattedText().contains("has left the party.")) {
            handlePartyList();
            PartyContainer partyContainer = PlayerInfo.get(SocialData.class).getPlayerParty();

            String member = component.getUnformattedText().split(" has left the party.")[0];
            partyContainer.removeMember(member);
        }
    }

}
