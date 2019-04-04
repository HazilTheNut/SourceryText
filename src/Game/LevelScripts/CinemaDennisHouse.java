package Game.LevelScripts;

import Data.SerializationVersion;

public class CinemaDennisHouse extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String[] getMaskNames() {
        return new String[]{"Awaken","Warning"};
    }

    private boolean awakenConvoHappened = false;
    private boolean warningConvoHappened = false;

    @Override
    public void onTurnEnd() {
        if (getMaskDataAt("Awaken", gi.getPlayer().getLocation()) && !awakenConvoHappened){
            awakenConvoHappened = true;
            gi.getTextBox().showMessage("Hunh?<p1> OH! You're awake!<np>" +
                    "You must be pretty dazzled, yeah? All these colors?<p1><nl> If you ask me, it's disgusting.<np>" +
                    "Someone went down The Source - that giant pit just outside my house - and got to change the universe's source code. Made everything all colorful and whatnot.<np>" +
                    "As a side effect, it knocked the lights out of everybody! Must've hit you extra hard, 'cause it took you a long while to wake up.<np>" +
                    "Well, at least you seem to have made it all in one piece. All those wolves and bandits out there would'a spelled your end!<np>" +
                    "That means you owe me your life, don't it? Hmm, how about you change things back! I find all these new colors to be totally tacky!<np>" +
                    "You'll need to fetch two things: <cy>The Documentation<cw>, which lets you figure out how to rewrite the universe, and a <cc>magic rope<cw> which can withstand exiting the universe. Without it, you have no way of getting back out of The Source, which makes it suicide.", "Dennis");
        }
        if (getMaskDataAt("Warning", gi.getPlayer().getLocation()) && !warningConvoHappened){
            warningConvoHappened = true;
            gi.getTextBox().showMessage("Oh, forgot to mention you something:<nl>You ever heard of the <cc>Colorful Coalition<cw>?<np>" +
                    "They might try to convince you to not revert the changes; just ignore them.", "Dennis");
        }
    }
}
