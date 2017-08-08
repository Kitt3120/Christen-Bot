package de.vanillekeks.christenbot.modules.modulemanager;

import de.vanillekeks.christenbot.Core;
import de.vanillekeks.christenbot.modules.Command;
import de.vanillekeks.christenbot.modules.IModule;
import de.vanillekeks.christenbot.modules.modulemanager.exceptions.ModuleNotFoundException;
import de.vanillekeks.christenbot.modules.modules.Audio;
import de.vanillekeks.christenbot.modules.modules.GameChanger;
import de.vanillekeks.christenbot.modules.modules.Update;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager implements EventListener {

    public List<IModule> modules = new ArrayList<IModule>();

    /*
     * Called when bot starts
     * Used to register all Modules
     * !Add new modules here!
     */
    public ModuleManager() {
        final ModuleManager manager = this;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(2000L);
                    Core.getBot().addEventListener(manager);

                    //Register modules
                    register(new GameChanger());
                    register(new Audio());
                    register(new Update());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void register(IModule module) {
        modules.add(module);
        module.onRegister();
    }

    public void unloadModule(IModule module) {
        if (isModuleLoaded(module)) {
            modules.remove(module);
            module.unload();
        }
    }

    @SuppressWarnings("unused")
    private boolean isModuleLoaded(String name) {
        for (IModule module : modules) {
            if (module.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private boolean isModuleLoaded(IModule module) {
        return modules.contains(module);
    }

    public IModule getModule(String name) throws ModuleNotFoundException {
        for (IModule module : modules) {
            if (module.getName().equalsIgnoreCase(name)) return module;
        }
        throw new ModuleNotFoundException("Module " + name + " not found");
    }

    public List<IModule> getModules() {
        return modules;
    }

    public boolean onCommand(MessageReceivedEvent event) {
        User author = event.getAuthor();
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();

        if (!channel.getType().equals(ChannelType.PRIVATE) && !channel.equals(Core.getMainTextChannel()))
            return true;

        String msg = message.getContent();
        if (msg.startsWith("!")) {
            String cmd = msg.contains(" ") ? msg.split(" ")[0].replaceFirst("!", "") : msg.replaceFirst("!", "");
            List<String> args = new ArrayList<>();
            for (String arg : msg.split(" ")) {
                if (!arg.startsWith("!") && !arg.startsWith("@")) args.add(arg);
            }

            boolean hasFound = false;
            for (IModule module : modules) {
                for (Command command : module.getCommands()) {
                    if (command.getTriggers().contains(cmd.toLowerCase())) {
                        hasFound = true;
                        module.onCommand(command, args, author, channel, message);
                    }
                }
            }

            if (!hasFound) {
                channel.sendMessage("Ich habe keinen Befehl gefunden, der " + cmd.toLowerCase() + " lautet").queue();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof MessageReceivedEvent) {
            if (onCommand((MessageReceivedEvent) event)) return;
        }
        for (IModule module : modules) {
            module.onEvent(event);
        }
    }

    public void shutdown() {
        for (IModule module : modules) {
            module.onShutdown();
        }
    }
}
// <3 Hi :3 Watashi wa anata o aishite