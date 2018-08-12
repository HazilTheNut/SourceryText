package Game.LevelScripts;

public class CinemaDennisHouse extends LevelScript {

    @Override
    public String[] getMaskNames() {
        return new String[]{"Conversation"};
    }

    private boolean conversationHappened = false;

    @Override
    public void onTurnEnd() {
        if (getMaskDataAt("Conversation", gi.getPlayer().getLocation()) && !conversationHappened){
            conversationHappened = true;
            gi.getTextBox().showMessage("Oh hey, yer awake.<p1> Long coma, huh?<p1><nl>Well, I guess that means ye owe me a couple.<nl>Too many bandits and wolves out there, ye would've died!<np>Anyway, I need you to do me a favor.<nl>Ya see all this colorful stuff? It's too complicated; I liked it much better when things were simple. <np>Just outside my house is The Source, a giant pit deep enough to exit out of the universe. I know of a magic rope strong enough to survive the exit, but it's protected by an ancient temple in a far away land.<nl>I know it's a bit of a stretch, but you seem pretty capable.<np>Besides, ya owe me yer life! It's the least you can do.", "Dennis");
        }
    }
}
