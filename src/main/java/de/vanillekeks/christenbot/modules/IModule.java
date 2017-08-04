package de.vanillekeks.christenbot.modules;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;

import java.util.List;

public interface IModule {

    //Commands
    public List<Command> getCommands();

    public boolean hasCommands();

    public void onCommand(Command command, List<String> args, User author, MessageChannel channel, Message message);

    //Name
    public String getName();

    //Misc
    public void onRegister();

    public void unload();

    public boolean isVisible();

    public void onEvent(Event event);

}
