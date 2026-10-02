import json, os, random
from PIL import Image

R = "src/main/resources"
def w(path, data, raw=False):
    p = os.path.join(R, path)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w", encoding="utf-8") as f:
        f.write(data if raw else json.dumps(data, indent=2, ensure_ascii=False) + "\n")

# ---------------- metadata
w("pack.mcmeta", {"pack": {"description": "Nubi", "pack_format": 15}})
w("META-INF/mods.toml", '''modLoader="javafml"
loaderVersion="${loader_version_range}"
license="${mod_license}"

[[mods]]
modId="${mod_id}"
version="${mod_version}"
displayName="${mod_name}"
authors="${mod_authors}"
description=\'\'\'${mod_description}\'\'\'

[[dependencies.${mod_id}]]
    modId="forge"
    mandatory=true
    versionRange="${forge_version_range}"
    ordering="NONE"
    side="BOTH"

[[dependencies.${mod_id}]]
    modId="minecraft"
    mandatory=true
    versionRange="${minecraft_version_range}"
    ordering="NONE"
    side="BOTH"
''', raw=True)

# ---------------- lang
es = {
 "itemGroup.nubi": "Nubi",
 "entity.nubi.nubi": "Nubi",
 "item.nubi.nubi_spawn_egg": "Generar Nubi",
 "item.nubi.magic_moisture_drop": "Gota de humedad mágica",
 "item.nubi.magic_moisture_lead": "Lazo de gota de humedad mágica",
 "item.nubi.cloud_helmet": "Casco de nube",
 "item.nubi.cloud_chestplate": "Peto de nube",
 "item.nubi.cloud_leggings": "Pantalones de nube",
 "item.nubi.cloud_boots": "Botas de nube",
 "key.nubi.float": "Flotar (armadura de nube)",
 "key.categories.nubi": "Nubi",
 "biome.nubi.mundo_nubi": "Mundo Nubi",
}
en = {
 "itemGroup.nubi": "Nubi",
 "entity.nubi.nubi": "Nubi",
 "item.nubi.nubi_spawn_egg": "Nubi Spawn Egg",
 "item.nubi.magic_moisture_drop": "Magic Moisture Drop",
 "item.nubi.magic_moisture_lead": "Magic Moisture Drop Lead",
 "item.nubi.cloud_helmet": "Cloud Helmet",
 "item.nubi.cloud_chestplate": "Cloud Chestplate",
 "item.nubi.cloud_leggings": "Cloud Leggings",
 "item.nubi.cloud_boots": "Cloud Boots",
 "key.nubi.float": "Float (cloud armor)",
 "key.categories.nubi": "Nubi",
 "biome.nubi.mundo_nubi": "Nubi World",
}
w("assets/nubi/lang/es_es.json", es)
w("assets/nubi/lang/en_us.json", en)

# ---------------- item models
for name in ["magic_moisture_drop","magic_moisture_lead","cloud_helmet","cloud_chestplate","cloud_leggings","cloud_boots"]:
    w(f"assets/nubi/models/item/{name}.json", {"parent":"minecraft:item/generated","textures":{"layer0":f"nubi:item/{name}"}})
w("assets/nubi/models/item/nubi_spawn_egg.json", {"parent":"minecraft:item/template_spawn_egg"})

# ---------------- recipes
def shaped(name, pattern, result, count=1):
    w(f"data/nubi/recipes/{name}.json", {
        "type":"minecraft:crafting_shaped","category":"equipment",
        "key":{"X":{"item":"nubi:magic_moisture_drop"}},
        "pattern":pattern,"result":{"item":f"nubi:{result}","count":count}})
shaped("cloud_helmet", ["XXX","X X"], "cloud_helmet")
shaped("cloud_chestplate", ["X X","XXX","XXX"], "cloud_chestplate")
shaped("cloud_leggings", ["XXX","X X","X X"], "cloud_leggings")
shaped("cloud_boots", ["X X","X X"], "cloud_boots")
w("data/nubi/recipes/magic_moisture_lead.json", {
    "type":"minecraft:crafting_shaped","category":"misc",
    "key":{"S":{"item":"minecraft:string"},"D":{"item":"nubi:magic_moisture_drop"}},
    "pattern":["SS ","SD ","  S"],"result":{"item":"nubi:magic_moisture_lead","count":1}})

# ---------------- loot
w("data/nubi/loot_tables/entities/nubi.json", {"type":"minecraft:entity","pools":[]})

# ---------------- spawns (blancas por todo el Overworld)
w("data/nubi/forge/biome_modifier/nubi_spawns.json", {
    "type":"forge:add_spawns","biomes":"#minecraft:is_overworld",
    "spawners":{"type":"nubi:nubi","weight":8,"minCount":2,"maxCount":4}})

# ---------------- bioma Mundo Nubi
def rgb(r,g,b): return (r<<16)|(g<<8)|b
w("data/nubi/worldgen/biome/mundo_nubi.json", {
  "has_precipitation": False,
  "temperature": 0.8,
  "downfall": 0.4,
  "effects": {
    "sky_color": rgb(255,208,240),
    "fog_color": rgb(255,232,250),
    "water_color": rgb(110,182,255),
    "water_fog_color": rgb(42,95,181)
  },
  "spawners": {
    "monster": [], "creature": [{"type":"nubi:nubi","weight":25,"minCount":3,"maxCount":6}],
    "ambient": [], "axolotls": [], "underground_water_creature": [],
    "water_creature": [], "water_ambient": [], "misc": []
  },
  "spawn_costs": {},
  "carvers": {},
  "features": [[] for _ in range(11)]
})

# ---------------- dimension Mundo Nubi (OPCIONAL: se puede borrar la carpeta dimension y dimension_type)
w("data/nubi/dimension_type/mundo_nubi.json", {
  "ultrawarm": False,"natural": True,"coordinate_scale": 1.0,"has_skylight": True,"has_ceiling": False,
  "ambient_light": 0.1,"piglin_safe": False,"bed_works": True,"respawn_anchor_works": False,"has_raids": False,
  "logical_height": 256,"min_y": 0,"height": 256,
  "infiniburn": "#minecraft:infiniburn_overworld","effects": "minecraft:overworld",
  "monster_spawn_light_level": {"type":"minecraft:uniform","value":{"min_inclusive":0,"max_inclusive":7}},
  "monster_spawn_block_light_limit": 0
})
w("data/nubi/dimension/mundo_nubi.json", {
  "type":"nubi:mundo_nubi",
  "generator":{"type":"minecraft:noise","settings":"minecraft:floating_islands",
               "biome_source":{"type":"minecraft:fixed","biome":"nubi:mundo_nubi"}}})

# =====================================================  TEXTURAS
T = "assets/nubi/textures"
def save(img, path):
    p = os.path.join(R, T, path); os.makedirs(os.path.dirname(p), exist_ok=True); img.save(p)

PAL = {  # base, s1, s2, top, bottom
 "white":  [(246,250,255),(232,243,255),(214,230,250),(255,255,255),(196,216,242)],
 "pink":   [(255,222,238),(255,204,226),(246,180,210),(255,240,248),(226,160,194)],
 "blue":   [(214,234,255),(190,220,255),(160,200,248),(236,246,255),(130,172,226)],
 "yellow": [(255,244,200),(255,234,160),(250,216,120),(255,252,226),(226,190,90)],
 "storm":  [(132,136,152),(112,116,134),(92,96,114),(156,160,176),(70,74,92)],
}
NAVY=(24,32,110); BLUE=(58,96,214); WHITE=(255,255,255); LBLUE=(140,176,255)

def regions():
    """devuelve dict (x,y)->'top'|'bottom'|'side' para la hoja 64x64 del modelo"""
    reg = {}
    def cube(u,v,wd,h,d):
        for x in range(u+d,u+d+wd):
            for y in range(v,v+d): reg[(x,y)]="top"
        for x in range(u+d+wd,u+d+2*wd):
            for y in range(v,v+d): reg[(x,y)]="bottom"
        for x in range(u,u+2*d+2*wd):
            for y in range(v+d,v+d+h):
                reg.setdefault((x,y),"side")
    cube(0,0,12,10,12)
    for (u,v) in [(0,24),(32,24),(0,40),(32,40)]: cube(u,v,8,8,8)
    return reg

def make_entity(kind):
    rnd = random.Random(hash(kind) & 0xffff)
    base,s1,s2,top,bot = PAL[kind]
    img = Image.new("RGBA",(64,64),(0,0,0,0))
    cells = {(x//2,y//2): rnd.random() for x in range(64) for y in range(64)}
    for (x,y),r in regions().items():
        c = cells[(x//2,y//2)]
        if r=="top": col = top if c<0.6 else base
        elif r=="bottom": col = bot if c<0.5 else s2
        else: col = base if c<0.55 else (s1 if c<0.85 else s2)
        img.putpixel((x,y), col+(255,))
    # cara: frente del cuerpo en (12..24, 12..22)
    fx,fy = 12,12
    def px(dx,dy,col): img.putpixel((fx+dx,fy+dy), col+(255,))
    for dx in range(12):
        for dy in range(10):
            px(dx,dy, base if kind!="storm" else s1)
    eye = [".NN.","NWBN","NBBN","NBLN",".NN."]
    cmap = {"N":NAVY,"W":WHITE,"B":BLUE,"L":LBLUE}
    for ex in (1,7):
        for j,row in enumerate(eye):
            for i,ch in enumerate(row):
                if ch in cmap: px(ex+i, 2+j, cmap[ch])
    if kind=="storm":
        for (dx,dy) in [(1,0),(2,1),(3,1),(10,0),(9,1),(8,1)]: px(dx,dy,NAVY)
        for (dx,dy) in [(4,8),(7,8),(5,7),(6,7)]: px(dx,dy,(60,50,80))
    else:
        blush = (255,150,185) if kind!="pink" else (240,110,155)
        for dx in (0,1,2,9,10,11):
            px(dx,7,blush); 
        for dx in (0,1,10,11): px(dx,8,blush)
        for (dx,dy) in [(4,7),(7,7),(5,8),(6,8)]: px(dx,dy,(200,96,130))
    save(img, f"entity/nubi_{kind}.png")

for k in PAL: make_entity(k)

# --------- items
def drop():
    rows = [
    "................",
    ".......NN.......",
    "......NBBN......",
    "......NBBN......",
    ".....NBLBBN.....",
    ".....NBLBBN.....",
    "....NBLBBBBN....",
    "....NBLBBBBN....",
    "...NBBLBBBBBN...",
    "...NBBBBBBBBN...",
    "...NBBBBBBBDN...",
    "...NBBBBBBDDN...",
    "....NBBBBDDN....",
    ".....NBDDDN.....",
    "......NNNN......",
    "................"]
    cm = {"N":(20,40,120),"B":(70,160,255),"L":(190,230,255),"D":(40,110,220)}
    img = Image.new("RGBA",(16,16),(0,0,0,0))
    for y,row in enumerate(rows):
        for x,ch in enumerate(row):
            if ch in cm: img.putpixel((x,y), cm[ch]+(255,))
    return img
d = drop(); save(d,"item/magic_moisture_drop.png")

def lead():
    img = Image.new("RGBA",(16,16),(0,0,0,0))
    brown=(150,100,50); dark=(100,65,30); light=(190,140,80)
    # anillo de cuerda
    import math
    cx,cy,r = 6.5,6.0,4.5
    for a in range(0,360,6):
        x = int(round(cx+r*math.cos(math.radians(a)))); y=int(round(cy+r*math.sin(math.radians(a))))
        col = light if (a//30)%2==0 else brown
        img.putpixel((x,y),col+(255,))
    for a in range(0,360,12):
        x = int(round(cx+(r+1)*math.cos(math.radians(a)))); y=int(round(cy+(r+1)*math.sin(math.radians(a))))
        if img.getpixel((x,y))[3]==0: img.putpixel((x,y),dark+(255,))
    # cola de cuerda
    for i,(x,y) in enumerate([(3,10),(2,11),(2,12),(1,13),(1,14)]): img.putpixel((x,y),(light if i%2==0 else brown)+(255,))
    # gotita
    small = [
     "..N..",".NBN.","NBLBN","NBBBN",".NNN."]
    cm = {"N":(20,40,120),"B":(70,160,255),"L":(190,230,255)}
    for j,row in enumerate(small):
        for i,ch in enumerate(row):
            if ch in cm: img.putpixel((9+i,10+j),cm[ch]+(255,))
    return img
save(lead(),"item/magic_moisture_lead.png")

SHAPES = {
"cloud_helmet":[
"................","................","....XXXXXXXX....","...XXXXXXXXXX...","...XXXXXXXXXX...",
"...XXXXXXXXXX...","...XXX....XXX...","...XXX....XXX...","...XX......XX...","................",
"................","................","................","................","................","................"],
"cloud_chestplate":[
"................","..XXX......XXX..",".XXXXX....XXXXX.",".XXXXXXXXXXXXXX.",".XXXXXXXXXXXXXX.",
"..XXXXXXXXXXXX..","...XXXXXXXXXX...","...XXXXXXXXXX...","...XXXXXXXXXX...","...XXXXXXXXXX...",
"...XXXXXXXXXX...","....XXXXXXXX....","................","................","................","................"],
"cloud_leggings":[
"................","..XXXXXXXXXXXX..","..XXXXXXXXXXXX..","..XXXXXXXXXXXX..","..XXXXX..XXXXX..",
"..XXXX....XXXX..","..XXXX....XXXX..","..XXXX....XXXX..","..XXXX....XXXX..","..XXXX....XXXX..",
"................","................","................","................","................","................"],
"cloud_boots":[
"................","................","................","................","................",
"...XXXX..XXXX...","...XXXX..XXXX...","...XXXX..XXXX...","..XXXXX..XXXXX..",".XXXXXXX.XXXXXXX",
"................","................","................","................","................","................"],
}
def armor_icon(name):
    rows = SHAPES[name]
    mask = {(x,y) for y,r in enumerate(rows) for x,c in enumerate(r) if c=="X"}
    img = Image.new("RGBA",(16,16),(0,0,0,0))
    rnd = random.Random(name)
    ys = [y for _,y in mask]; y0,y1=min(ys),max(ys)
    for (x,y) in mask:
        edge = any((x+dx,y+dy) not in mask for dx,dy in [(1,0),(-1,0),(0,1),(0,-1)])
        if edge: col=(70,110,190)
        else:
            t = (y-y0)/max(1,(y1-y0))
            col=(250,252,255) if t<0.35 else ((232,243,255) if t<0.7 else (206,226,250))
            if rnd.random()<0.18: col=(190,218,248)
        img.putpixel((x,y),col+(255,))
    return img
for n in SHAPES: save(armor_icon(n), f"item/{n}.png")

# --------- capas de armadura (64x32)
def armor_layer(name, seed):
    rnd = random.Random(seed)
    img = Image.new("RGBA",(64,32))
    cells = {(x//2,y//2): rnd.random() for x in range(64) for y in range(32)}
    for x in range(64):
        for y in range(32):
            c = cells[(x//2,y//2)]
            col = (250,252,255) if c<0.5 else ((232,243,255) if c<0.8 else (206,226,250))
            if y%8==7: col=(190,216,246)   # leve sombreado inferior por franjas
            img.putpixel((x,y),col+(255,))
    save(img, f"models/armor/{name}.png")
armor_layer("cloud_layer_1",1); armor_layer("cloud_layer_2",2)
print("resources ok")
