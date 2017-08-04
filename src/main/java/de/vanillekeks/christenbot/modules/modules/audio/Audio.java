package de.vanillekeks.christenbot.modules.modules.audio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.vanillekeks.christenbot.Core;
import de.vanillekeks.christenbot.misc.AdminChecker;
import de.vanillekeks.christenbot.modules.Command;
import de.vanillekeks.christenbot.modules.IModule;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.managers.AudioManager;

public class Audio implements IModule, EventListener {

    private List<Command> commands = new ArrayList<>();
    private User lockedUser;
    
    private AudioManager audioManager;
    
    public Audio() {
    	audioManager = Core.getBot().getGuildById("339619850362159104").getAudioManager();
    	audioManager.setSendingHandler(Core.getAudioSystem());
    	
		Core.getBot().addEventListener(this);		
        commands.add(new Command("Lock", null));
        commands.add(new Command("Unlock", null));
        commands.add(new Command("Search", null));
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
        if (commands.get(0).equals(command)) {
            if (lockedUser != null) {
                if (lockedUser == author) {
                    channel.sendMessage("Ich folge dir bereits auf Schritt und Tritt, " + author.getName()).queue();
                    return;
                }
                if (!AdminChecker.isAdmin(author)) {
                    channel.sendMessage("Entschuldige, aber ich bin schon auf einer Reise mit " + lockedUser.getName()).queue();
                    return;
                } else {
                    channel.sendMessage("Nun denn, ich unterbreche meine Reise mit " + lockedUser.getName()).queue();
                }
            }
            lockedUser = author;
            channel.sendMessage("Ich freue mich, diese Reise mit dir anzutreten, " + lockedUser.getName() + ". Ich werde deinem weiten Weg folgen.").queue();

            joinChannel();
        }
        if (commands.get(1).equals(command)) {
            if (lockedUser == null) {
                channel.sendMessage("Ich bin zurzeit auf keiner Reise, schreibe !lock, damit ich dir folge, " + author.getName()).queue();
                return;
            } else {
                if (lockedUser == author || AdminChecker.isAdmin(author)) {
                    lockedUser = null;
                    joinChannel();
                    channel.sendMessage("So sei es, hier werden sich unsere Wege trennen").queue();
                } else {
                    channel.sendMessage("Ich bin schon auf einer Reise mit " + lockedUser.getName()).queue();
                }
            }
        }
        if (commands.get(2).equals(command)) {
            if (args.size() == 0) {
                channel.sendMessage("Entschuldige, aber du hast keinen Text für die Suchanfrage angegeben").queue();
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                for (String arg : args) {
                    stringBuilder.append(" " + arg);
                }
                String searchTerm = stringBuilder.toString().replaceFirst(" ", "");
                try {
                    new AudioRequest(searchTerm, channel, author, Core.getYoutube());
                } catch (IOException e) {
                    e.printStackTrace();
                    channel.sendMessage("Entschuldige, aber es gab einen Fehler bei der Anfrage").queue();
                }
            }
        }
    }

    private void joinChannel() {
        if (lockedUser == null) {
            disconnect();
            return;
        } else {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                for (Member member : voiceChannel.getMembers()) {
                    if (member.getUser().equals(lockedUser)) {
                        audioManager.openAudioConnection(voiceChannel);
                        return;
                    }
                }
            }

            disconnect();
        }
    }

    private void disconnect() {
        if (audioManager.isConnected()) audioManager.closeAudioConnection();
    }

    @Override
    public String getName() {
        return "Lock/Unlock";
    }

    @Override
    public void onRegister() {
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof GuildVoiceJoinEvent) {
            if (((GuildVoiceJoinEvent) event).getMember().getUser().equals(lockedUser)) joinChannel();
        }
        if (event instanceof GuildVoiceLeaveEvent) {
            if (((GuildVoiceLeaveEvent) event).getMember().getUser().equals(lockedUser)) joinChannel();
        }
        if (event instanceof GuildVoiceMoveEvent) {
            if (((GuildVoiceMoveEvent) event).getMember().getUser().equals(lockedUser)) joinChannel();
        }
    }

}
