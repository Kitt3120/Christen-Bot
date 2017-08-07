package de.vanillekeks.christenbot.modules.modules.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.vanillekeks.christenbot.Core;
import de.vanillekeks.christenbot.audio.QueueSizeTooSmallException;
import de.vanillekeks.christenbot.audio.TrackNotInQueueException;
import de.vanillekeks.christenbot.audio.TrackType;
import de.vanillekeks.christenbot.misc.AdminChecker;
import de.vanillekeks.christenbot.modules.Command;
import de.vanillekeks.christenbot.modules.IModule;
import de.vanillekeks.christenbot.requests.AudioRequest;
import de.vanillekeks.christenbot.requests.SkipRequest;
import de.vanillekeks.christenbot.requests.VoteRequest;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.requests.RestAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Audio implements IModule, EventListener {

    private List<Command> commands = new ArrayList<>();
    private User lockedUser;

    private boolean skipRequestRunning = false;
    
    public Audio() {
		Core.getBot().addEventListener(this);
        commands.add(new Command("Lock", "Lässt den Bot deinem Channel joinen", null));
        commands.add(new Command("Unlock", "Gibt den Bot wieder frei", null));
        commands.add(new Command("Queue", "Zeigt die Warteschleife", null));
        commands.add(new Command("Search", "Sucht auf Youtube nach Suchbegriffen", null));
        commands.add(new Command("Play", "Sucht auf einer Seite nach Songs", null));
        commands.add(new Command("Volume", "Zeigt die Lautstärke", null));
        commands.add(new Command("Skip", "Startet eine Skip-Anfrage", null));
        commands.add(new Command("Discard", "Startet eine Anfrage, einen bevorstehenden Song aus der Warteschleife zu entfernen", null));
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
            List<AudioTrack> queue = Core.getAudioSystem().getTrackScheduler().getQueue();
            if (Core.getAudioSystem().getTrackScheduler().getQueue().isEmpty()) {
                channel.sendMessage("```Die Warteschlange ist leer```").queue();
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                int count = 1;
                for (AudioTrack track : queue) {
                    stringBuilder.append("[" + count + "] - " + track.getInfo().title + "\n");
                    count++;
                }
                channel.sendMessage("```\n" + stringBuilder.toString() + "```").queue();
            }
        }
        if (commands.get(3).equals(command)) {
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
        if (commands.get(4).equals(command)) {
            if (args.size() == 0) {
                channel.sendMessage("Entschuldige, aber du hast keinen Link angegeben").queue();
            } else {
                String link = args.get(0);
                RestAction<Message> action = channel.sendMessage("```Lädt...```");
                Message msg = null;
                try {
                    msg = action.complete(true);
                } catch (RateLimitedException e) {
                    e.printStackTrace();
                    channel.sendMessage("Es ist etwas schiefgelaufen").queue();
                    return;
                }

                final Message finalMsg = msg;
                Core.getAudioSystem().play(link, TrackType.LINK, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack audioTrack) {
                        Core.getAudioSystem().getTrackScheduler().queue(audioTrack);
                        finalMsg.editMessage("```" + audioTrack.getInfo().title + " zur Warteschlange hinzugefügt```").queue();
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist audioPlaylist) {
                        for (AudioTrack track : audioPlaylist.getTracks()) {
                            Core.getAudioSystem().getTrackScheduler().queue(track);
                        }
                        finalMsg.editMessage("```" + audioPlaylist.getTracks().size() + " Songs zur Warteschlange hinzugefügt```").queue();
                    }

                    @Override
                    public void noMatches() {
                        finalMsg.editMessage("```Keine Songs unter diesem Link gefunden```").queue();
                    }

                    @Override
                    public void loadFailed(FriendlyException e) {
                        finalMsg.editMessage("```Ich konnte die Songs auf dieser Seite nicht laden```").queue();
                    }
                });
            }
        }
        if (commands.get(5).equals(command)) {
            if (args.size() == 0) {
                int volume = Core.getAudioSystem().getAudioPlayer().getVolume();
                int missing = 100 - volume;
                StringBuilder barBuilder = new StringBuilder().append("[");
                for (int i = 0; i < volume; i++) barBuilder.append("=");
                for (int i = 0; i < missing; i++) barBuilder.append("-");
                barBuilder.append("]");

                channel.sendMessage("```Die Lautstärke betragt zurzeit " + volume + "%\n" + barBuilder.toString() + "```").queue();
            } else {
                if (!AdminChecker.isAdmin(author)) {
                    channel.sendMessage("Es tut mir leid, doch du darfst dies nicht tun, " + author.getName()).queue();
                } else {
                    int volume;
                    try {
                        volume = Integer.parseInt(args.get(0));
                    } catch (Exception e) {
                        channel.sendMessage(args.get(0) + " ist keine gültige Lautstärke").queue();
                        return;
                    }
                    if (volume > 100 || volume < 1) {
                        channel.sendMessage("Bitte gebe eine Lautstärke von 1-100 an").queue();
                        return;
                    }
                    Core.getAudioSystem().getAudioPlayer().setVolume(volume);
                    int missing = 100 - volume;
                    StringBuilder barBuilder = new StringBuilder().append("[");
                    for (int i = 0; i < volume; i++) barBuilder.append("=");
                    for (int i = 0; i < missing; i++) barBuilder.append("-");
                    barBuilder.append("]");

                    channel.sendMessage("Lautstärke auf " + volume + "% gesetzt\n```" + barBuilder.toString() + "```").queue();
                }
            }
        }
        if (commands.get(6).equals(command)) {
            if (skipRequestRunning) {
                channel.sendMessage("Es sei dir gewiss, dass bereits eine Skip-Anfrage läuft").queue();
                return;
            }
            if (args.size() == 0) {
                skipRequestRunning = true;
                new SkipRequest(author, channel) {
                    @Override
                    public void onFinish() {
                        skipRequestRunning = false;
                    }
                };
            } else {
                String arg = args.get(0);
                if (arg.equalsIgnoreCase("admin")) {
                    if (AdminChecker.isAdmin(author)) {
                        SkipRequest request = new SkipRequest(author, channel) {
                            @Override
                            public void onFinish() {
                            }
                        };
                        request.setVotesToSuccess(1);
                    } else {
                        channel.sendMessage("Entschuldige, " + author.getName() + ", doch ich darf dir dies leider nicht erlauben").queue();
                    }
                } else {
                    channel.sendMessage("Unbekanntes Argument").queue();
                }
            }
        }
        if (commands.get(7).equals(command)) {
            if (args.size() == 0) {
                channel.sendMessage("Benutze den Befehl so: !Discard <Zahl>\nBenutze !Queue, um dir die Warteschleife anzeigen zu lassen").queue();
            } else {
                int number;
                try {
                    number = Integer.parseInt(args.get(0));
                } catch (Exception e) {
                    channel.sendMessage(args.get(0) + " ist keine gültige Zahl").queue();
                    return;
                }
                number--;
                if (number <= 0) {
                    channel.sendMessage("Dieser Song kann zurzeit nicht entfernt werden. Benutze !Skip").queue();
                    return;
                }
                if (Core.getAudioSystem().getTrackScheduler().getQueue().size() < number) {
                    channel.sendMessage("Es sind nur " + Core.getAudioSystem().getTrackScheduler().getQueue().size() + " Songs in der Warteschleife").queue();
                } else {
                    final AudioTrack track = Core.getAudioSystem().getTrackScheduler().getQueue().get(number);
                    new VoteRequest(author, channel, 30, "Song \"" + track.getInfo().title + "\" aus der Warteschleife entfernen") {
                        @Override
                        public void onTimeUp(int positiveVotes, int negativeVotes, int totalVotes, double positivePercentage, double negativePercentage, boolean isPositive) {
                            if (isPositive) {
                                try {
                                    Core.getAudioSystem().getTrackScheduler().remove(Core.getAudioSystem().getTrackScheduler().getTrackIdByTrack(track));
                                } catch (QueueSizeTooSmallException e) {
                                    e.printStackTrace();
                                } catch (TrackNotInQueueException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                }
            }
        }
    }

    private void joinChannel() {
        if (lockedUser == null) {
            disconnect();
            return;
        } else {
            for (VoiceChannel voiceChannel : Core.getAudioSystem().getAudioManager().getGuild().getVoiceChannels()) {
                for (Member member : voiceChannel.getMembers()) {
                    if (member.getUser().equals(lockedUser)) {
                        Core.getAudioSystem().getAudioManager().openAudioConnection(voiceChannel);
                        return;
                    }
                }
            }

            disconnect();
        }
    }

    private void disconnect() {
        if (Core.getAudioSystem().getAudioManager().isConnected())
            Core.getAudioSystem().getAudioManager().closeAudioConnection();
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
