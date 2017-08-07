package de.vanillekeks.christenbot.modules.modules;

import de.vanillekeks.christenbot.Core;
import de.vanillekeks.christenbot.misc.AdminChecker;
import de.vanillekeks.christenbot.modules.Command;
import de.vanillekeks.christenbot.modules.IModule;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;

import java.util.ArrayList;
import java.util.List;

public class GameChanger implements IModule {

    private List<Command> commands = new ArrayList<>();

    public GameChanger() {
        commands.add(new Command("ChangeGame", "Ändert das Spiel, das der Bot spielt", new String[]{"CG", "ChangeG", "CGame"}));
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public boolean hasCommands() {
        return true;
    }

    @Override
    public void onCommand(Command command, List<String> args, User author, MessageChannel channel, Message message) {
        if (command.equals(commands.get(0))) {
            if (!AdminChecker.isAdmin(author)) {
                channel.sendMessage("Entschuldige, " + author.getName() + ", doch ich darf dir dies leider nicht erlauben.").queue();
            } else {
                if (args.size() == 0) {
                    channel.sendMessage("Entschuldige, " + author.getName() + ", doch du hast kein Spiel angegeben, welches ich spielen soll").queue();
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (String arg : args) {
                        sb.append(" " + arg);
                    }
                    final String gameName = sb.toString().replaceFirst(" ", "");
                    channel.sendMessage("Ich werde mich in die Abenteuer vom Spiel " + gameName + " st�rzen!").queue();
                    Core.getBot().getPresence().setPresence(OnlineStatus.ONLINE, new Game() {

                        @Override
                        public String getUrl() {
                            return "https://discord.gg/h7VgrS4";
                        }

                        @Override
                        public GameType getType() {
                            return GameType.DEFAULT;
                        }

                        @Override
                        public String getName() {
                            return gameName;
                        }
                    }, false);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "GameChanger";
    }

    @Override
    public void onRegister() {
        Core.getBot().getPresence().setPresence(OnlineStatus.ONLINE, new Game() {

            @Override
            public String getUrl() {
                return "https://discord.gg/h7VgrS4";
            }

            @Override
            public GameType getType() {
                return GameType.DEFAULT;
            }

            @Override
            public String getName() {
                return "die Reise nach Jerusalem";
            }
        }, false);
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void onEvent(Event event) {
        // TODO Auto-generated method stub

    }

}
