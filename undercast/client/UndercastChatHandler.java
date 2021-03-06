package undercast.client;
//You may not release this source under any condition, it must be linked to this page
//You may recompile and publish as long as skipperguy12 and Guru_Fraser are given credit
//You may not claim this to be your own
//You may not remove these comments

import undercast.client.achievements.UndercastKillsHandler;
import net.minecraft.src.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.mod_Undercast;

public class UndercastChatHandler {
    public UndercastChatHandler(String message, String username, EntityPlayer player, String unstripedMessage) {
        //Friend tracking Joining.
        if (message.contains(" joined the game")) {
            String name;
            String server;
            message = message.replace(" joined the game", "");
            if (message.contains("[")) {
                name = message.split(" ")[1];
                server = message.split(" ")[0].replace("[", "").replace("]", "");
            } else {
                name = message.substring(message.lastIndexOf("*") + 1, message.length());
                server = UndercastData.server;
            }
            if (UndercastData.friends.containsKey(name)) {
                UndercastData.friends.put(name, server);
            }
        }
        //friend tracking. Leaving
        else if (message.contains("left the game")) {
            String name;
            String server;
            message = message.replace(" left the game", "");
            if (message.contains("[")) {
                name = message.split(" ")[1];
                server = message.split(" ")[0].replace("[", "").replace("]", "");
            } else {
                name = message.substring(message.lastIndexOf("*") + 1, message.length());
                server = UndercastData.server;
            }
            if (UndercastData.friends.containsKey(name)) {
                if (UndercastData.friends.get(name).equals(server)) {
                    UndercastData.friends.put(name, "offline");
                }
            }
        }
        // friend tracking - switching
        else if(message.contains(" changed servers")) {
            String  name;
            String server;
            message = message.replace(" changed servers", "");
            name = message.substring(message.indexOf("]") + 2);
            server = message.substring(message.indexOf("� ") + 2, message.indexOf("]"));
            if(UndercastData.friends.containsKey(name)) {
                UndercastData.friends.put(name, server);
            }
        }
        //update what map you are playing on
        else if (message.contains("Now playing")) {
            message = message.replace("Now playing ", "");
            UndercastData.setMap((message.split(" by ")[0]));
            if(UndercastData.getKills() == 0 && UndercastData.getDeaths() == 0) { // new match or observer or noob
                UndercastData.reloadServerInformations(false);
                UndercastData.reloadStats();
            }
        }
        //if you die
        else if (message.startsWith(username) && !message.toLowerCase().endsWith(" team")) {
            // if you die form someone
            if((message.contains(" by ") || message.contains(" took ") || message.contains(" fury of"))) {
                if(message.contains(" by ") && UndercastCustomMethods.isTeamkill(unstripedMessage, username, message.substring(message.indexOf("by") + 3, message.lastIndexOf("'s") == -1 ? message.length() : message.lastIndexOf("'s")))) {
                    return;
                }
                UndercastData.addKilled(1);
            }
                UndercastData.addDeaths(1);
                UndercastData.setPreviousKillstreak((int) UndercastData.getKillstreak());
                UndercastData.resetKillstreak();
        }
        //if you kill a person
        else if ((message.contains("by " + username) && !message.toLowerCase().contains(" destroyed by ")) || message.contains("took " + username) || message.contains("fury of " + username)) {
            if(!UndercastCustomMethods.isTeamkill(unstripedMessage, username, message.substring(0, message.indexOf(" ")))) {
                UndercastData.addKills(1);
                UndercastData.addKillstreak(1);
            }
        }
        else if(message.startsWith(username + " scored") && message.toLowerCase().contains(" team")) {
            int score;
            try {
                score = Integer.parseInt(message.substring(message.indexOf(" scored ") + 8, message.indexOf(" points")));
            } catch(Exception e){
                score = 0;
            }
            UndercastData.addScore(score);
        }
        //when you join a match
        else if (message.contains("You joined the")) {
            UndercastData.reloadStats();
            try {
                UndercastData.setTeam(UndercastData.Teams.valueOf(message.replace("You joined the ", "").replace(" Team", "").replace(" team", "")));
            } catch(Exception e) {
                // if the team set fails because of an alias, set the team to Unknown
                UndercastData.setTeam(UndercastData.Teams.Unknown);
            }
        }
        //when a map is done. Display all the stats
        else if (!message.startsWith("<") && message.toLowerCase().contains("cycling to") && message.contains("1 second")) {
            player.addChatMessage("\u00A7m-\u00A7m-\u00A7m-\u00A7m-\u00A7m-\u00A7m-\u00A7m-\u00A7m-\u00A7m-\u00A7m-");
            player.addChatMessage("Final Stats:");
            player.addChatMessage("\u00A7m-\u00A7m-\u00A7m-\u00A7m-\u00A7m-\u00A7m-\u00A7m-\u00A7m-\u00A7m-\u00A7m-");
            player.addChatMessage("Kills: " + (int)UndercastData.getKills() + ((mod_Undercast.CONFIG.realtimeStats) ? (" Total: " + (int)(UndercastData.kills + UndercastData.stats.kills)) : ""));
            player.addChatMessage("Deaths: " + (int)UndercastData.getDeaths() + ((mod_Undercast.CONFIG.realtimeStats) ? (" Total: " + (int)(UndercastData.deaths + UndercastData.stats.deaths)) : ""));
            player.addChatMessage("K/D: " + UndercastCustomMethods.getKD());
            player.addChatMessage("Kill Streak: " + (int)UndercastData.getLargestKillstreak());
            UndercastData.resetKills();
            UndercastData.resetKilled();
            UndercastData.resetDeaths();
            UndercastData.resetKillstreak();
            UndercastData.resetLargestKillstreak();
            UndercastData.resetScore();
            UndercastData.setTeam(UndercastData.Teams.Observers);
        }
        //filters [Tip] messages
        else if (message.startsWith("[Tip]") && mod_Undercast.CONFIG.filterTips) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().deleteChatLine(0);
        }
        // redirection and lobby detection
        else if(message.contains("Welcome to the Overcast Network")){
            if(UndercastData.redirect) {
                UndercastData.redirect = false;
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/server " + UndercastData.directionServer);
            } else {
                UndercastData.setServer("Lobby");
                UndercastCustomMethods.handleServerSwap();
            }
            if(mod_Undercast.CONFIG.showFriends) {
                Thread t1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            mod_Undercast.friendHandler.isListening = true;
                            Minecraft.getMinecraft().thePlayer.sendChatMessage("/fr");
                        } catch (InterruptedException ex) {
                        }
                    }
                });
                t1.start();
            }
        }
        //server detection
        else if(message.contains("Teleporting you to ")) {
            UndercastData.setServer(message.replace("Teleporting you to ", ""));
            if(!message.toLowerCase().contains("lobby")) {
                UndercastData.welcomeMessageExpected = true;
            }
            UndercastCustomMethods.handleServerSwap();
        } else if(message.contains("Connecting to ")) {
            UndercastData.setServer(message.replace("Connecting to ", ""));
            if(!message.toLowerCase().contains("lobby")) {
                UndercastData.welcomeMessageExpected = true;
            }
            UndercastCustomMethods.handleServerSwap();
        } else if(message.contains("You are currently on ")) {
            if(UndercastData.serverDetectionCommandExecuted) {
                UndercastData.serverDetectionCommandExecuted = false;
                UndercastData.setServer(message.replace("You are currently on ", ""));
                UndercastCustomMethods.handleServerSwap();
            }
        } else if(message.toLowerCase().contains("game over")) {
            UndercastData.isGameOver = true;
            UndercastData.isNextKillFirstBlood = false;
            try {
                // stop the timer
                UndercastData.matchTimer.stop();
            } catch (Exception ignored) {
            }
        } else if(message.toLowerCase().contains("the match has started")) {
            UndercastData.isGameOver = false;
            UndercastData.isNextKillFirstBlood = true;
            UndercastData.reloadStats();
            
            // stop the timer
            try {
                UndercastData.matchTimer.stop();
            } catch (Exception ignored) {
            }
            //and start one which starts from 0
            UndercastData.incrementMatchTime = true;
            UndercastData.matchTimeHours = 0;
            UndercastData.matchTimeMin = 0;
            UndercastData.matchTimeSec = 0;
            UndercastData.matchTimer = new MatchTimer();
            
        } else if(message.equals("                    ")) {
            if(!UndercastData.welcomeMessageExpected) {
                UndercastData.serverDetectionCommandExecuted = true;
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/server");
            } else {
                UndercastData.welcomeMessageExpected = false;
            }
            if(mod_Undercast.CONFIG.matchOnServerJoin || mod_Undercast.CONFIG.showMatchTime) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/match");
            }
        // start and sync the match timer
        } else if(message.toLowerCase().contains("time:") || message.toLowerCase().contains("score:") || message.toLowerCase().contains("time remaining: ")) {
            String time = "-2:-2";
            String messageToReplace;
            // stop the timer
            try {
                UndercastData.matchTimer.stop();
            } catch (Exception ignored) {
            }
            // extract the time
            messageToReplace = message.split("[0-9]{1,2}[:]{1}[0-5]?[0-9]{1}[:]?[0-5]?[0-9]?")[0];
            time = message.replace(messageToReplace, "");
            
            // detect if it should increment or decrement
            if(messageToReplace.toLowerCase().contains("time:")) {
                UndercastData.incrementMatchTime = true;
            } else {
                UndercastData.incrementMatchTime = false;
            }
            
            // read the time
            String[] numbers = time.split("[:]{1}");
            if(numbers.length == 3) {
                UndercastData.matchTimeHours = Integer.parseInt(numbers[0]);
                UndercastData.matchTimeMin = Integer.parseInt(numbers[1]);
                UndercastData.matchTimeSec = Integer.parseInt(numbers[2]);
            } else {
                UndercastData.matchTimeHours = 0;
                UndercastData.matchTimeMin = Integer.parseInt(numbers[0]);
                UndercastData.matchTimeSec = Integer.parseInt(numbers[1]);
            }
            // start the timer
            UndercastData.matchTimer = new MatchTimer();
        } else if(message.startsWith("Current class:")) {
            UndercastData.currentGSClass = message.replace("Current class: ", "");
        } else if(message.startsWith("You have selected")){
            UndercastData.currentGSClass = message.replace("You have selected ", "");
        } else if(message.startsWith("You have chosen: ")){
            UndercastData.currentGSClass = message.replace("You have chosen: ", "");
        }
    }
}
