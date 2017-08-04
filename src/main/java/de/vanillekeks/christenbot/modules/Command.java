package de.vanillekeks.christenbot.modules;

import java.util.ArrayList;
import java.util.List;

public class Command {

    private String command;
    private List<String> aliases = new ArrayList<>();

    private List<String> triggers = new ArrayList<>();

    public Command(String command, String[] aliases) {
        this.command = command;

        if (aliases != null) {
            for (String alias : aliases) {
                this.aliases.add(alias);
            }
        }

        triggers.add(this.command.toLowerCase());
        for (String alias : this.aliases) {
            triggers.add(alias.toLowerCase());
        }
    }

    public String getCommand() {
        return command;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public List<String> getTriggers() {
        return triggers;
    }

}
