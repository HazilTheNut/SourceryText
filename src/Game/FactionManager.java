package Game;

import Data.SerializationVersion;
import Game.Debug.DebugWindow;
import Game.Entities.GameCharacter;
import Game.Registries.TagRegistry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class FactionManager implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<Faction> factions;

    public void initialize(){
        factions = new ArrayList<>();

        createFaction("player");
        createFaction("monster", new FactionOpinion("ALL", -10));
        createFaction("bandit", new FactionOpinion("villager", -3), new FactionOpinion("player", -3));
        createFaction("villager", new FactionOpinion("bandit", -3));
        createFaction("wizard"); //Should go hostile to bandits when launching flare
        createFaction("termite", new FactionOpinion("spider", -3));
        createFaction("spider", new FactionOpinion("termite", -3));
        createFaction("virtuous", new FactionOpinion("vicious", -3));
        createFaction("vicious", new FactionOpinion("virtuous", -3));
        createFaction("colorful", new FactionOpinion("player", -3));
        createFaction("antipirate", new FactionOpinion("vicious", -3));
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

    public Faction getFaction(String name){
        for (Faction faction : factions){
            if (name.equals(faction.name))
                return faction;
        }
        return null;
    }

    public ArrayList<Faction> getFactions() {
        return factions;
    }

    public byte getOpinion(GameCharacter source, GameCharacter target){
        byte totalOpinion = 0;
        for (String factionName : source.getFactionAlignments()){
            Faction faction = getFaction(factionName); //Gets all factions the source is a member of
            if (faction != null){
                for (FactionOpinion factionOpinion : faction.relations){ //Gets all the opinions each faction alignment of the source has
                    if (factionOpinion.name.equals("ALL") || target.getFactionAlignments().contains(factionOpinion.name)){ //If the target's allegiance matches the opinion
                        totalOpinion += factionOpinion.opinion;
                    }
                }
                if (target.getFactionAlignments().contains(faction.name))
                    totalOpinion += 7;
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

    public class Faction implements Serializable{

        private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

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

        public void addOpinion(String name, int opinion){
            for (FactionOpinion relation : relations){
                if (relation.name.equals(name)){
                    relation.opinion = (byte)Math.max(-10, Math.min(relation.opinion + opinion, 10));
                    return;
                }
            }
            relations.add(new FactionOpinion(name, opinion));
        }

        public String getName() {
            return name;
        }
    }

    private class FactionOpinion implements Serializable {

        private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

        /**
         * FactionOpinion:
         *
         * Describes a singular "opinion" that a faction has about another one.
         *
         * Here's a guideline for the opinions:
         *
         * -3 and below: Attacks
         * +3 and above: Friendly, able to communicate
         * -2 to +2    : Neutral
         *
         * Killing someone = -2 to opinion
         * Membership of a faction = +7 to opinion
         *
         * Opinions stop compounding if the value goes beyond +/-10
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
