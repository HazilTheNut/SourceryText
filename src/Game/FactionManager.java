package Game;

import Game.Debug.DebugWindow;
import Game.Entities.GameCharacter;
import Game.Registries.TagRegistry;

import java.util.ArrayList;
import java.util.Arrays;

public class FactionManager {

    private ArrayList<Faction> factions;

    void initialize(){
        factions = new ArrayList<>();

        createFaction("stranger");
        createFaction("blueshirt", new FactionOpinion("redshirt", -1));
        createFaction("redshirt", new FactionOpinion("blueshirt", -1), new FactionOpinion("stranger", -1));
        createFaction("greenshirt", new FactionOpinion("blueshirt", 1), new FactionOpinion("redshirt", 1));
    }

    /**
     * Takes an input String and generates an ArrayList of faction names found in that string
     *
     * @param factionList The input string, like "stranger, bandit, wizard"
     * @return The output ArrayList
     */
    public ArrayList<String> extractFactionList(String factionList){
        ArrayList<String> output = new ArrayList<>();
        for (Faction faction : factions){
            if (factionList.contains(faction.name))
                output.add(faction.name);
        }
        return output;
    }

    private Faction getFaction(String name){
        for (Faction faction : factions){
            if (name.equals(faction.name))
                return faction;
        }
        return null;
    }

    public byte getOpinion(GameCharacter source, GameCharacter target){
        byte totalOpinion = 0;
        for (String factionName : source.getFactionAlignments()){
            Faction faction = getFaction(factionName);
            if (faction != null){
                for (String otherFaction : target.getFactionAlignments()){
                    totalOpinion += faction.getOpinionOf(otherFaction);
                }
            }
        }
        if (source.hasTag(TagRegistry.BERSERK))
            return (byte)(-1 * totalOpinion);
        return totalOpinion;
    }

    private void createFaction(String name, FactionOpinion... opinions){
        ArrayList<FactionOpinion> factionOpinions = new ArrayList<>(Arrays.asList(opinions));
        Faction faction = new Faction(name, factionOpinions);
        factions.add(faction);
        DebugWindow.reportf(DebugWindow.MISC, "FactionManager.createFaction", faction.toString());
    }

    private class Faction {
        /**
         * Faction:
         *
         * Describes a specific game faction, with a list of "opinions" of other factions.
         */
        private String name;
        private ArrayList<FactionOpinion> relations;

        private Faction(String name, ArrayList<FactionOpinion> opinions){
            this.name = name;
            relations = opinions;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("Faction: \"");
            builder.append(name).append("\" opinions:");
            for (FactionOpinion opinion : relations){
                builder.append(opinion.toString());
            }
            return builder.toString();
        }

        private byte getOpinionOf(String factionName){
            for (FactionOpinion relation : relations){
                if (relation.name.equals(factionName))
                    return relation.opinion;
            }
            return 0;
        }
    }

    private class FactionOpinion {
        /**
         * FactionOpinion:
         *
         * Describes a singular "opinion" that a faction has about another one.
         *
         * Here's a guideline for the opinions:
         *
         * -3 : Unforgivable, you heathen
         * -2 : I will attack you, and there is no way you will join us
         * -1 : I will attack you, but you can join us if you want to
         * 0  : Who are you again?
         * +1 : I like you, consider joining us
         * +2 : You are our friend
         * +3 : It's as if you are a member of our group
         */
        private String name; //The name of the faction this opinion is of; i.e. "I dislike this faction by x amount"
        private byte opinion; //Should generally sit in the -3 to 3 range

        private FactionOpinion(String name, int opinion){
            this.name = name;
            this.opinion = (byte)opinion;
        }

        @Override
        public String toString() {
            return String.format(" {%1$s:%2$d}", name, opinion);
        }
    }
}
