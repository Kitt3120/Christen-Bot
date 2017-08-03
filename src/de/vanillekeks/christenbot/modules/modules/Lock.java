package de.vanillekeks.christenbot.modules.modules;

import de.vanillekeks.christenbot.Core;
import de.vanillekeks.christenbot.misc.AdminChecker;
import de.vanillekeks.christenbot.modules.Command;
import de.vanillekeks.christenbot.modules.IModule;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.ArrayList;
import java.util.List;

public class Lock implements IModule {
	
	private List<Command> commands = new ArrayList<>();
	private User lockedUser;

	private AudioManager audioManager;
	
	public Lock() {
		audioManager = Core.getInstance().getBot().getGuildById("339619850362159104").getAudioManager(); //Vatikan https://discord.gg/h7VgrS4
		commands.add(new Command("Lock", null));
		commands.add(new Command("Unlock", null));
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
		if(commands.get(0).equals(command)) {
			if(lockedUser != null) {
				if(lockedUser == author) {
					channel.sendMessage("Ich folge dir bereits auf Schritt und Tritt, " + author.getName()).queue();
					return;
				}
				if(!AdminChecker.isAdmin(author)) {
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
	}

	private void joinChannel() {
		if(lockedUser == null) {
			disconnect();
			return;
		} else {
			for(VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
				for(Member member : voiceChannel.getMembers()) {
					if(member.getUser().equals(lockedUser)){
						audioManager.openAudioConnection(voiceChannel);
						return;
					}
				}
			}

			disconnect();
		}
	}

	private void disconnect() {
		if(audioManager.isConnected()) audioManager.closeAudioConnection();
	}

	@Override
	public String getName() {
		return "Lock/Unlock";
	}

	@Override
	public void onRegister() {}

	@Override
	public void unload() {}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public void onEvent(Event event) {
		if(event instanceof GuildVoiceJoinEvent) {
			if(((GuildVoiceJoinEvent) event).getMember().getUser().equals(lockedUser)) joinChannel();
		}
		if(event instanceof GuildVoiceLeaveEvent) {
			if(((GuildVoiceLeaveEvent) event).getMember().getUser().equals(lockedUser)) joinChannel();
		}
	}

}
