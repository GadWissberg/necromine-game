import os.path
import bpy
import glob

PARENT = "C:\\Users\\gadw1\\StudioProjects\\isometric-game\\materials\\sprites\\anubis"
types = ("Idle", "Run", "Attack Melee", "Pain")
general_types = ("Idle", "Run", "Attack Melee","Attack Primary", "Pain", "@Light Death 1", "@Light Death 2","@Light Death 3")
subfolders = ()
TYPE_IDLE = "idle"
PADDING = "###"
cameras = ("South", "North", "South West", "West", "North West", "North East", "East", "South East")


def find_marker_by_name(name):
    for marker in bpy.context.scene.timeline_markers:
        if marker.name == name:
            return marker
    return None


def get_marker_index(marker):
    for i in range(0, len(bpy.context.scene.timeline_markers)):
        if bpy.context.scene.timeline_markers[i].name == marker.name:
            return i
    return -1


def render_type_direction(sub_folder, spr_type, selected_camera, include_direction_in_name=True):
    _initialize_output_path(include_direction_in_name, selected_camera, spr_type, sub_folder)
    path_wild_card = bpy.context.scene.render.filepath.replace(PADDING, '*')
    if not glob.glob(path_wild_card):
        bpy.context.scene.camera = bpy.context.scene.objects[selected_camera]
        if sub_folder is not None:
            current_marker = find_marker_by_name(sub_folder + " " + spr_type)
        else:
            current_marker = find_marker_by_name(spr_type)
        marker_index = get_marker_index(current_marker)
        next_marker = None
        if marker_index < len(bpy.context.scene.timeline_markers) - 1:
            next_marker = bpy.context.scene.timeline_markers[marker_index + 1]
        bpy.context.scene.frame_start = current_marker.frame
        if next_marker is not None:
            bpy.context.scene.frame_end = next_marker.frame - 1
            bpy.ops.render.render(animation=True)
        output_frames = glob.glob(path_wild_card)
        for i in range(len(output_frames)):
            output = output_frames[i]
            os.rename(output, output[:-(4 + len(PADDING))] + str(i) + ".png")


def _initialize_output_path(include_direction_in_name, selected_camera, spr_type, sub_folder):
    spr_type = spr_type.replace(' ', '_')
    direction_name = selected_camera.lower().replace(' ', '_')
    if "west" in direction_name:
        direction_name = direction_name.replace("west", "east")
    elif "east" in direction_name:
        direction_name = direction_name.replace("east", "west")
    bpy.context.scene.render.filepath = PARENT + "\\"
    if sub_folder is not None:
        bpy.context.scene.render.filepath += sub_folder.lower() + "\\"
    bpy.context.scene.render.filepath += spr_type.lower()
    if include_direction_in_name:
        bpy.context.scene.render.filepath += "_" + direction_name
    bpy.context.scene.render.filepath += "_" + PADDING


def export_types(type_list, sub_folder_path):
    for sprite_type in type_list:
        if sprite_type[0] != '@':
            for camera in cameras:
                render_type_direction(sub_folder_path, sprite_type, camera)
        else:
            render_type_direction(sub_folder_path, sprite_type[1:], "South West", False)


def main():
    print("=======================================================================================================")
    for subfolder in subfolders:
        export_types(types, subfolder)
    export_types(general_types, None)


if __name__ == "__main__":
    main()
