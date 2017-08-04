package de.vanillekeks.christenbot.modules.modulemanager;

import de.vanillekeks.christenbot.Core;
import de.vanillekeks.christenbot.modules.Command;
import de.vanillekeks.christenbot.modules.IModule;
import de.vanillekeks.christenbot.modules.modulemanager.exceptions.ModuleNotFoundException;
import de.vanillekeks.christenbot.modules.modules.GameChanger;
import de.vanillekeks.christenbot.modules.modules.audio.Audio;
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
                    Core.getInstance().getBot().addEventListener(manager);

                    //Register modules
                    register(new GameChanger());
                    register(new Audio());
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

    public boolean onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();

        String msg = message.getContent();
        if (msg.startsWith("!")) {
            String cmd = msg.contains(" ") ? msg.split(" ")[0].replaceFirst("!", "") : msg.replaceFirst("!", "");
            List<String> args = new ArrayList<>();
            for (String arg : msg.split(" ")) {
                if (!arg.startsWith("!") && !arg.startsWith("@")) args.add(arg);
            }

            for (IModule module : modules) {
                for (Command command : module.getCommands()) {
                    if (command.getTriggers().contains(cmd.toLowerCase())) {
                        module.onCommand(command, args, author, channel, message);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof MessageReceivedEvent) {
            if (onMessageReceived((MessageReceivedEvent) event)) {
                return;
            }
        }
        for (IModule module : modules) {
            module.onEvent(event);
        }
    }

}
// <3 Hi :3 Watashi wa anata o aishite