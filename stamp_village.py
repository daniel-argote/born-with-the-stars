
import math

def read_map(file_path):
    with open(file_path, 'r') as f:
        lines = f.readlines()
    # Assuming map is 100x100 and each tile is separated by a space or is a single character
    # Let's handle both cases: space-separated or single character per line.
    # We'll split by space if there are spaces, otherwise treat each char as a tile.
    game_map = []
    for line in lines:
        line = line.strip()
        if not line:
            continue
        # Check if tiles are space-separated or just characters next to each other
        if ' ' in line:
            game_map.append(line.split(' '))
        else:
            game_map.append(list(line))
    return game_map

def write_map(file_path, game_map):
    with open(file_path, 'w') as f:
        for row in game_map:
            f.write(' '.join(row) + '\n')


def stamp_village(game_map, center_x, center_y):
    # Place campfire at the center
    game_map[center_y][center_x] = '5'

    longhouse_width = 3
    longhouse_height = 2
    radius = 7 # Distance from center to the center of a longhouse group
    num_longhouses = 6

    for i in range(num_longhouses):
        angle = 2 * math.pi * i / num_longhouses
        
        # Calculate approximate center of the longhouse group
        lh_center_x = int(center_x + radius * math.cos(angle))
        lh_center_y = int(center_y + radius * math.sin(angle))

        # Adjust top-left corner of the longhouse based on its center
        lh_start_x = lh_center_x - longhouse_width // 2
        lh_start_y = lh_center_y - longhouse_height // 2
        
        # Ensure coordinates are within bounds
        for y_offset in range(longhouse_height):
            for x_offset in range(longhouse_width):
                current_x = lh_start_x + x_offset
                current_y = lh_start_y + y_offset
                if 0 <= current_y < len(game_map) and 0 <= current_x < len(game_map[0]):
                    game_map[current_y][current_x] = '2'

    return game_map

if __name__ == '__main__':
    map_file = 'res/maps/world_map.txt'
    center_x = 50
    center_y = 50

    original_map = read_map(map_file)
    
    # Ensure the map is 100x100 for proper stamping
    if len(original_map) != 100 or len(original_map[0]) != 100:
        print(f"Error: Expected a 100x100 map, but found {len(original_map)}x{len(original_map[0])}.")
        print("Please ensure 'res/maps/world_map.txt' is a 100x100 grid.")
    else:
        modified_map = stamp_village(original_map, center_x, center_y)
        write_map(map_file, modified_map)
        print(f"Village stamped successfully at ({center_x}, {center_y}) in {map_file}")
