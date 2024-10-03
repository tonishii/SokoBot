package solver;

import java.util.LinkedList;
import java.util.List;
import reader.MapData;

public class StateNode {
    private MapData mapState;
    private int value;
    private List<StateNode> children;

    public StateNode(MapData data, int value) {
        this.mapState = data;
        this.value = value;
        this.children = new LinkedList<>();
    }

    public void addChild(StateNode state) {
        this.children.add(state);
    }
    public MapData getMapState() {
        return mapState;
    }
    public int getValue() {
        return value;
    }
    public List<StateNode> getChildren() {
        return children;
    }
}