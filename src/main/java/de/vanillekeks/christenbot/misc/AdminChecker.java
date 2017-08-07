package de.vanillekeks.christenbot.misc;

import net.dv8tion.jda.core.entities.User;

public class AdminChecker {

    private static String papstId = "256510610165071873";
    private static String vanId = "186105158076203011";
    private static String kittId = "238645059329720330";
    private static String semihId = "292269732873240597";

    public static boolean isAdmin(User user) {
        return (user.getId().equals(papstId) || user.getId().equals(vanId) || user.getId().equals(kittId) || user.getId().equals(semihId));
    }

}
