package main;

import java.util.ArrayList;
import main.GamePanel;
import entity.Entity;

public class PathFinder {

    GamePanel gp;
    Node[][] node;
    ArrayList<Node> openList = new ArrayList<>();
    public ArrayList<Node> pathList = new ArrayList<>();
    Node startNode, goalNode, currentNode;
    boolean goalReached = false;
    int step = 0;

    public PathFinder(GamePanel gp) {
        this.gp = gp;
        instantiateNodes();
    }

    public void instantiateNodes() {
        node = new Node[gp.maxWorldCol][gp.maxWorldRow];
        for(int col = 0; col < gp.maxWorldCol; col++) {
            for(int row = 0; row < gp.maxWorldRow; row++) {
                node[col][row] = new Node(col, row);
            }
        }
    }

    public void resetNodes() {
        for(int col = 0; col < gp.maxWorldCol; col++) {
            for(int row = 0; row < gp.maxWorldRow; row++) {
                node[col][row].open = false;
                node[col][row].checked = false;
                node[col][row].solid = false;
            }
        }
        openList.clear();
        pathList.clear();
        goalReached = false;
        step = 0;
    }

    public void setNodes(int startCol, int startRow, int goalCol, int goalRow) {
        resetNodes();

        startNode = node[startCol][startRow];
        currentNode = startNode;
        goalNode = node[goalCol][goalRow];
        openList.add(currentNode);

        for(int col = 0; col < gp.maxWorldCol; col++) {
            for(int row = 0; row < gp.maxWorldRow; row++) {
                // 1. CHECK TILES
                int tileNum = gp.tileM.mapTileNum[gp.currentMap][col][row];
                if(gp.tileM.tileCache.get(tileNum).collision) {
                    node[col][row].solid = true;
                }
                // Check Fog Map - Cannot pathfind into unknown lands
                if (gp.tileM.fogMap[gp.currentMap][col][row]) {
                    node[col][row].solid = true;
                }
            }
        }
        
        // 2. CHECK OBJECTS (Trees, etc.)
        for(Entity e : gp.obj) {
            if(e != null && e.collision && e.map == gp.currentMap) {
                // Calculate center of object to get grid position
                int col = (e.worldX + e.solidArea.x) / gp.tileSize;
                int row = (e.worldY + e.solidArea.y) / gp.tileSize;
                if(col >= 0 && col < gp.maxWorldCol && row >= 0 && row < gp.maxWorldRow) {
                    node[col][row].solid = true;
                }
            }
        }
    }

    public boolean search() {
        while(!goalReached && step < 500) {
            int col = currentNode.col;
            int row = currentNode.row;

            currentNode.checked = true;
            openList.remove(currentNode);

            if(row - 1 >= 0) openNode(node[col][row-1]);
            if(col - 1 >= 0) openNode(node[col-1][row]);
            if(row + 1 < gp.maxWorldRow) openNode(node[col][row+1]);
            if(col + 1 < gp.maxWorldCol) openNode(node[col+1][row]);

            int bestNodeIndex = 0;
            int bestNodefCost = 999;

            for(int i = 0; i < openList.size(); i++) {
                if(openList.get(i).fCost < bestNodefCost) {
                    bestNodeIndex = i;
                    bestNodefCost = openList.get(i).fCost;
                }
                else if(openList.get(i).fCost == bestNodefCost) {
                    if(openList.get(i).gCost < openList.get(bestNodeIndex).gCost) {
                        bestNodeIndex = i;
                    }
                }
            }

            if(openList.size() == 0) break;

            currentNode = openList.get(bestNodeIndex);

            if(currentNode == goalNode) {
                goalReached = true;
                trackThePath();
            }
            step++;
        }
        return goalReached;
    }

    public void openNode(Node node) {
        if(!node.open && !node.checked && !node.solid) {
            node.open = true;
            node.parent = currentNode;
            node.gCost = currentNode.gCost + 1;
            node.hCost = Math.abs(node.col - goalNode.col) + Math.abs(node.row - goalNode.row);
            node.fCost = node.gCost + node.hCost;
            openList.add(node);
        }
    }

    public void trackThePath() {
        Node current = goalNode;
        while(current != startNode) {
            pathList.add(0, current);
            current = current.parent;
        }
    }
}