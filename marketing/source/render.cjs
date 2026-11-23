// Renders the listing artwork in marketing/images/ from real in-game screenshots (marketing/images/screenshots/), the
// mod's own item textures, isometric renders of its block models (marketing/source/renders/, made by renders.py) and
// the bundled OFL fonts. Usage (from the repository root):
//   NODE_PATH="$(npm root -g)" node marketing/source/render.cjs [name...]
// Requires the `playwright` package with a Chromium build available. Set CHROMIUM to a Chromium executable to use
// that one instead of Playwright's own download.
const path = require('path');
const fs = require('fs');
const { chromium } = require('playwright');

// The release and the supported Minecraft versions. Adding a version is one line here
// (plus one row in the compatibility tables of curseforge/description.md and modrinth/description.md).
const RELEASE = { version: '1.0' };
const VERSIONS = [
  { minecraft: '1.20.1', loaders: ['Forge'] },
];
const LOADERS = ['Fabric', 'Forge', 'NeoForge'].filter((loader) => VERSIONS.some((v) => v.loaders.includes(loader)));

const root = path.resolve(__dirname, '..', '..');
const out = path.join(root, 'marketing', 'images');
const shots = path.join(out, 'screenshots');
const fonts = path.join(__dirname, 'fonts');
const renders = path.join(__dirname, 'renders');
const items = path.join(root, 'src', 'main', 'resources', 'assets', 'sweettreats', 'textures', 'item');
const url = (file) => 'file://' + file;
const shot = (name) => url(path.join(shots, name));
const source = (name) => url(path.join(__dirname, name));

// Names from the en_us lang file; effects and food values from registry/ModItems.java.
const NAMES = JSON.parse(fs.readFileSync(path.join(root, 'src/main/resources/assets/sweettreats/lang/en_us.json'), 'utf8'));
const nameOf = (id) => NAMES[`item.sweettreats.${id}`] || NAMES[`block.sweettreats.${id}`] || id;

const css = `
@font-face { font-family: StDisplay; src: url('${url(path.join(fonts, 'fredoka.ttf'))}'); }
@font-face { font-family: StText; src: url('${url(path.join(fonts, 'nunito.ttf'))}'); font-weight: 400; }
@font-face { font-family: StText; src: url('${url(path.join(fonts, 'nunito-extrabold.ttf'))}'); font-weight: 800; }
:root { --text:#5a3e44; --dim:#9a7f86; --cocoa:#6b3f3a; --berry:#ef5f8b; --berry-deep:#d43f6e; --mint:#5fbfa3; --mint-deep:#3f9c83;
        --lav:#9b7ce0; --peach:#f59e6b; --butter:#f6cf6a; --cream:#fff8f4; }
* { box-sizing: border-box; }
html, body { margin: 0; background: var(--cream); color: var(--text); font-family: StText, sans-serif; }
.stage { position: relative; overflow: hidden; background: linear-gradient(160deg, #fff8f4 0%, #fdeef2 55%, #eaf6f1 100%); }
.display { font-family: StDisplay, sans-serif; }
.candy { background: linear-gradient(100deg, #e64379 0%, #f2789c 40%, #c47ad8 75%, #8f78e3 100%);
         -webkit-background-clip: text; background-clip: text; color: transparent; }
.soft { filter: drop-shadow(0 3px 0 rgba(255,255,255,.95)) drop-shadow(0 10px 24px rgba(212,63,110,.25)); }
.kicker { font-family: StDisplay, sans-serif; color: var(--mint-deep); letter-spacing: .14em; font-size: 24px; text-transform: uppercase; }
.heart { display: inline-block; vertical-align: -4px; margin-right: 12px; }
.scallop { background: radial-gradient(circle at 12px -2px, transparent 12px, #f7a8c0 13px) 0 0 / 24px 14px repeat-x; }
.scallop.up { background: radial-gradient(circle at 12px 16px, transparent 12px, #f7a8c0 13px) 0 0 / 24px 14px repeat-x; }
.rule { height: 6px; border-radius: 3px; background: repeating-linear-gradient(90deg, var(--berry) 0 10px, transparent 10px 16px, var(--mint) 16px 26px, transparent 26px 32px, var(--lav) 32px 42px, transparent 42px 48px); opacity: .8; }
.frame { position: relative; border: 7px solid #fff; border-radius: 22px; overflow: hidden; background: #fff;
         box-shadow: 0 0 0 2px rgba(239,95,139,.5), 0 26px 60px rgba(107,63,58,.25), 0 6px 14px rgba(107,63,58,.12); }
.frame > img { display: block; width: 100%; }
canvas.motes { position: absolute; left: 0; top: 0; width: 100%; height: 100%; pointer-events: none; }
.wash { position: absolute; inset: 0; background: radial-gradient(ellipse at 70% 45%, rgba(255,255,255,0) 30%, rgba(255,246,246,.6) 100%); }
.backdrop { position: absolute; inset: -40px; background-size: cover; background-position: center; filter: blur(18px) brightness(1.12) saturate(1.05); }
.pill { display: inline-block; border-radius: 999px; padding: 9px 22px 7px; margin-right: 12px; font-family: StDisplay, sans-serif; font-size: 22px;
        color: #fff; background: linear-gradient(100deg, #ef5f8b, #c47ad8); box-shadow: 0 6px 16px rgba(212,63,110,.3); }
.pill.ghost { color: var(--mint-deep); background: rgba(255,255,255,.9); box-shadow: 0 0 0 2px rgba(63,156,131,.55), 0 6px 16px rgba(107,63,58,.12); }
.label { position: absolute; font-family: StDisplay, sans-serif; font-size: 23px; color: var(--cocoa); background: rgba(255,255,255,.95);
         border-radius: 999px; padding: 8px 20px 6px; white-space: nowrap; box-shadow: 0 0 0 2px rgba(239,95,139,.6), 0 10px 26px rgba(107,63,58,.25); z-index: 2; }
ul.points { list-style: none; padding: 0; margin: 0; }
ul.points li { position: relative; padding-left: 38px; margin: 0 0 16px; font-size: 27px; line-height: 1.3; color: var(--text); }
ul.points li::before { content: ''; position: absolute; left: 0; top: 8px; width: 22px; height: 22px; background: var(--berry);
  -webkit-mask: var(--heartmask) center / contain no-repeat; mask: var(--heartmask) center / contain no-repeat; }
ul.points li:nth-child(3n+2)::before { background: var(--lav); }
ul.points li:nth-child(3n)::before { background: var(--mint); }
:root { --heartmask: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'><path d='M12 21.5 3.2 12.9C.4 10.1.6 5.6 3.8 3.6 6.3 2 9.6 2.6 12 5.2 14.4 2.6 17.7 2 20.2 3.6c3.2 2 3.4 6.5.6 9.3z'/></svg>"); }
b.hi { color: var(--cocoa); font-weight: 800; }
.stats { display: flex; gap: 14px; margin-top: 22px; }
.stats > div { flex: 1; padding: 14px 20px 12px; border-radius: 18px; background: rgba(255,255,255,.93);
               box-shadow: 0 0 0 2px rgba(239,95,139,.4), 0 14px 34px rgba(107,63,58,.16); text-align: center; }
.stats .t { font-family: StText, sans-serif; font-weight: 800; font-size: 16px; letter-spacing: .1em; text-transform: uppercase; color: var(--dim); }
.stats .v { font-family: StDisplay, sans-serif; font-size: 34px; color: var(--cocoa); margin-top: 2px; white-space: nowrap; }
.tile { position: relative; border-radius: 18px; background: rgba(255,255,255,.93); text-align: center;
        box-shadow: 0 0 0 2px rgba(239,95,139,.38), 0 14px 30px rgba(107,63,58,.16); }
.tile .art { display: flex; align-items: center; justify-content: center; }
.tile .art img { image-rendering: pixelated; max-width: 100%; max-height: 100%; }
.tile .n { font-family: StDisplay, sans-serif; color: var(--cocoa); line-height: 1.1; }
.tile .s { font-family: StText, sans-serif; font-weight: 800; color: var(--dim); font-size: 15px; letter-spacing: .04em; }
pre.code { margin: 0; font-family: 'DejaVu Sans Mono', monospace; font-size: 21px; line-height: 1.5; color: var(--text); }
.arrow { font-family: StDisplay, sans-serif; font-size: 46px; color: var(--berry); }
`;

// A plump heart, the bakery counterpart of the sibling kits' flowers and stars.
const heartSvg = (fill, size) => `<svg class="heart" width="${size}" height="${size}" viewBox="0 0 24 24" style="display:inline-block">
  <path d="M12 21.5 3.2 12.9C.4 10.1.6 5.6 3.8 3.6 6.3 2 9.6 2.6 12 5.2 14.4 2.6 17.7 2 20.2 3.6c3.2 2 3.4 6.5.6 9.3z" fill="${fill}" stroke="#fff" stroke-width="1.6"/>
  <ellipse cx="7.4" cy="7.6" rx="2" ry="1.3" fill="#fff" opacity=".55" transform="rotate(-35 7.4 7.6)"/></svg>`;

// Floating sprinkles and little hearts. Deterministic so renders are reproducible.
const motes = (count = 44, seed = 7) => `<canvas class="motes"></canvas><script>
(() => { const c = document.currentScript.previousElementSibling; const r = c.getBoundingClientRect();
  c.width = r.width; c.height = r.height; const g = c.getContext('2d'); let s = ${seed};
  const rnd = () => (s = (s * 16807) % 2147483647) / 2147483647;
  const tints = ['239,95,139', '155,124,224', '95,191,163', '246,207,106', '245,158,107'];
  const heart = (x, y, z, a, tint) => { g.save(); g.translate(x, y); g.scale(z / 6, z / 6); g.fillStyle = 'rgba(' + tint + ',' + a + ')';
    g.beginPath(); g.moveTo(0, 5); g.bezierCurveTo(-8, -1, -5, -8, 0, -3.5); g.bezierCurveTo(5, -8, 8, -1, 0, 5); g.fill(); g.restore(); };
  const sprinkle = (x, y, z, a, rot, tint) => { g.save(); g.translate(x, y); g.rotate(rot); g.fillStyle = 'rgba(' + tint + ',' + a + ')';
    g.beginPath(); g.roundRect(-z * 2.2, -z * .6, z * 4.4, z * 1.2, z * .6); g.fill(); g.restore(); };
  for (let i = 0; i < ${count}; i++) { const x = rnd() * c.width, y = rnd() * c.height, a = .35 + rnd() * .4, z = 1.6 + rnd() * 2.2;
    const tint = tints[Math.floor(rnd() * tints.length)];
    if (rnd() < .28) heart(x, y, z * 2.4, a, tint); else sprinkle(x, y, z, a, rnd() * Math.PI, tint); } })();
</script>`;

// Waits for every image to load.
const loadScript = `<script>
(() => { const imgs = [...document.images]; let left = imgs.length;
  const done = () => { if (--left <= 0) document.body.dataset.items = 'ready'; };
  if (!left) document.body.dataset.items = 'ready';
  for (const i of imgs) { if (i.complete) done(); else { i.onload = done; i.onerror = done; } } })();
</script>`;
// An isometric render of a block model (renders.py), or an item's own 16x16 texture, drawn pixel-sharp.
const model = (name, h) => `<img src="${url(path.join(renders, `${name}.png`))}" style="height:${h}px;image-rendering:pixelated">`;
const sprite = (id, h) => `<img src="${url(path.join(items, `${id}.png`))}" style="width:${h}px;height:${h}px;image-rendering:pixelated">`;
// A tile with art and a caption.
const tile = (art, title, { art: artH = 140, size = 20, sub = '', pad = '12px 8px 12px' } = {}) => `<div class="tile" style="padding:${pad}">
    <div class="art" style="height:${artH}px">${art}</div>
    <div class="n" style="font-size:${size}px;margin-top:6px">${title}</div>${sub ? `<div class="s">${sub}</div>` : ''}</div>`;
const grid = (cols, cells, gap = 14) => `<div style="display:grid;grid-template-columns:repeat(${cols},1fr);gap:${gap}px">${cells.join('')}</div>`;

const page = (width, height, body, transparent = false) => `<!doctype html><html><head><meta charset="utf-8"><style>${css}
body { width: ${width}px; height: ${height}px; } .stage { width: ${width}px; height: ${height}px; }
${transparent ? 'html, body, .stage { background: transparent; }' : ''}</style></head>
<body><div class="stage">${body}</div>${loadScript}</body></html>`;

// A section heading: a candy-striped ribbon with the title, readable on CurseForge's and Modrinth's light and dark themes.
const header = (title) => page(1400, 120, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:22px">
    <div class="rule" style="width:230px;-webkit-mask-image:linear-gradient(90deg,transparent,#000);mask-image:linear-gradient(90deg,transparent,#000)"></div>
    ${heartSvg('#9b7ce0', 34)}
    <div style="position:relative;padding:14px 54px 12px;border-radius:999px;background:linear-gradient(100deg,#ef5f8b 0%,#f07ea0 45%,#c47ad8 100%);
                box-shadow:0 0 0 4px #fff,0 0 0 6px rgba(63,156,131,.7),0 12px 28px rgba(107,63,58,.28)">
      <div class="display" style="font-size:46px;line-height:1;color:#fff;white-space:nowrap;text-shadow:0 3px 0 rgba(160,40,90,.35)">${title}</div></div>
    ${heartSvg('#5fbfa3', 34)}
    <div class="rule" style="width:230px;-webkit-mask-image:linear-gradient(90deg,#000,transparent);mask-image:linear-gradient(90deg,#000,transparent)"></div>
  </div>`, true);

// A feature card: copy on the left, artwork on the right. Bullets never end on a one-word line.
const card = ({ kicker, title, points, art, width = 1600, height = 900, artWidth = 980 }) => page(width, height, `
  <div class="backdrop" style="background-image:url('${source('backdrop.jpg')}');opacity:.45"></div>
  <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(255,248,244,.96) 0%,rgba(255,248,244,.88) 34%,rgba(253,238,242,.6) 62%,rgba(234,246,241,.42) 100%)"></div>
  <div class="wash"></div>${motes()}
  <div class="scallop" style="position:absolute;left:0;right:0;top:0;height:14px"></div>
  <div class="scallop up" style="position:absolute;left:0;right:0;bottom:0;height:14px"></div>
  <div style="position:absolute;left:76px;top:0;bottom:0;width:${width - artWidth - 176}px;display:flex;flex-direction:column;justify-content:center">
    <div class="kicker"${kicker.length > 22 ? ' style="font-size:20px;letter-spacing:.1em"' : ''}>${heartSvg('#ef5f8b', 26)}${kicker}</div>
    <div class="display candy soft" style="font-size:66px;line-height:1.02;margin:16px 0 22px">${title}</div>
    <div class="rule" style="width:70%;margin-bottom:30px"></div>
    <ul class="points">${points.map((point) => `<li>${point.replace(/ (\S+)$/, '&nbsp;$1')}</li>`).join('')}</ul>
  </div>
  <div style="position:absolute;right:56px;top:50%;transform:translateY(-50%);width:${artWidth}px">${art}</div>`);

// An exact region (x, y, w, h in source pixels, 1920x1080 captures) of a screenshot, scaled to W pixels wide.
const region = (file, x, y, w, h, W, full = 1920) => `<div style="position:relative;width:${W}px;height:${Math.round(W * h / w)}px;overflow:hidden">
    <img src="${shot(file)}" style="position:absolute;max-width:none;width:${W * full / w}px;left:${-x * W / w}px;top:${-y * W / w}px;image-rendering:${w < 1000 ? 'pixelated' : 'auto'}"></div>`;
const framed = (inner, style = '') => `<div class="frame" style="${style}">${inner}</div>`;
const label = (text, style) => `<div class="label" style="${style}">${text}</div>`;
const stats = (list) => `<div class="stats">${list.map(([t, v]) => `<div><div class="v">${v}</div><div class="t">${t}</div></div>`).join('')}</div>`;
const pair = (first, second, firstLabel, secondLabel, height = 640, width = '73%') => `<div style="position:relative;height:${height}px">
    <div class="frame" style="position:absolute;left:0;top:0;width:${width}">${first}</div>
    <div class="frame" style="position:absolute;right:0;bottom:0;width:${width}">${second}</div>
    ${firstLabel ? label(firstLabel, 'left:24px;top:22px') : ''}${secondLabel ? label(secondLabel, 'right:24px;bottom:22px') : ''}</div>`;
const code = (file, body) => `<div class="frame" style="background:rgba(255,255,255,.96);padding:22px 32px">
  <div style="font-family:StText;font-weight:800;font-size:18px;letter-spacing:.08em;color:var(--dim);margin-bottom:8px">${file}</div><pre class="code">${body}</pre></div>`;
const k = (t) => `<span style="color:#d43f6e">${t}</span>`;
const v = (t) => `<span style="color:#3f9c83">${t}</span>`;
const cm = (t) => `<span style="color:#b9a3a8">${t}</span>`;
// One decorating recipe: base + toppings -> result, all as item sprites.
const plus = '<span class="arrow" style="font-size:36px;color:var(--dim)">+</span>';
const recipeRow = (base, toppings, result, count = 1) => `<div class="tile" style="display:flex;align-items:center;gap:16px;padding:10px 22px;justify-content:center">
    ${sprite(base, 64)}${toppings.map((t) => plus + sprite(t, 64)).join('')}<span class="arrow">&rarr;</span>${sprite(result, 80)}
    <div class="n" style="font-size:22px;text-align:left;width:230px">${count > 1 ? `${count}× ` : ''}${nameOf(result)}</div></div>`;

// Screen crops in the 1920x1080 captures (GUI scale 4): each machine's whole menu, the station's recipe row, the open book.
const OVEN = { x: 600, y: 200, w: 716, h: 676 };
const BOWL = OVEN;
const STATION = { x: 612, y: 212, w: 692, h: 262 };
const BOOK = { x: 440, y: 108, w: 1040, h: 836 };
const BOOKD = BOOK;

const versionText = VERSIONS.map((ver) => ver.minecraft).join(' · ');

const TREATS = ['strawberry_cake', 'pink_vanilla_cupcake', 'chocolate_cupcake', 'lavender_macaron', 'strawberry_macaron', 'heart_cookie',
  'cinnamon_roll', 'mini_donuts', 'peach_pastry', 'cherry_pie', 'heart_candy', 'strawberry_lollipop', 'marshmallow_cloud', 'rainbow_cookie',
  'honey_cupcake', 'moon_cookie'];
const DRINKS = [['strawberry_milk', 'Hands back the bottle'], ['vanilla_milkshake', 'Hands back the bottle'], ['lavender_tea', 'Regeneration 10s'],
  ['peach_tea', 'Haste 45s'], ['bubble_tea', 'Jump Boost 45s'], ['pink_lemonade', 'Speed 30s'], ['hot_chocolate', 'Resistance 30s'], ['rose_tea', 'Absorption 60s']];

const assets = {
  banner: () => page(1800, 600, `
    <img src="${shot('bakery.jpg')}" style="position:absolute;right:-20px;top:-30px;width:1120px;-webkit-mask-image:linear-gradient(90deg,transparent 0%,#000 30%);mask-image:linear-gradient(90deg,transparent 0%,#000 30%)">
    <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(255,248,244,.98) 0%,rgba(255,248,244,.96) 42%,rgba(255,248,244,.5) 60%,rgba(255,248,244,0) 76%)"></div>
    ${motes(50, 11)}
    <div class="scallop" style="position:absolute;left:0;right:0;top:0;height:16px"></div>
    <div class="scallop up" style="position:absolute;left:0;right:0;bottom:0;height:16px"></div>
    <div style="position:absolute;left:86px;top:58px;width:1000px">
      <div class="kicker">${heartSvg('#ef5f8b', 26)}Version ${RELEASE.version} · Sugarplum Studios</div>
      <div class="display candy soft" style="font-size:112px;line-height:1;white-space:nowrap;margin:16px 0 4px">Sweet Treats</div>
      <div class="rule" style="width:560px;margin:18px 0 24px"></div>
      <div style="font-family:StDisplay;font-size:42px;color:var(--cocoa);line-height:1.2">A cute bakery mod.</div>
      <div style="font-size:27px;color:var(--text);line-height:1.35;margin-top:8px">Bake pastel pastries, brew teas and mix milkshakes,<br>then show them off in your own bakery.</div>
      <div style="margin-top:30px">${LOADERS.map((l) => `<span class="pill">${l}</span>`).join('')}<span class="pill ghost">No dependencies</span></div>
      <div style="margin-top:16px;font-family:StDisplay;font-size:26px;color:var(--dim)">Minecraft ${versionText}</div>
    </div>
    <div style="position:absolute;left:880px;bottom:44px;display:flex;gap:14px;filter:drop-shadow(0 10px 16px rgba(107,63,58,.3))">
      ${['strawberry_cake', 'pink_vanilla_cupcake', 'bubble_tea', 'lavender_macaron'].map((id) => sprite(id, 84)).join('')}</div>
`),

  icon: () => page(512, 512, `
    <div style="position:absolute;inset:0;background:radial-gradient(circle at 50% 40%,#fff8f4 0%,#fde6ee 55%,#e2f3ec 100%)"></div>${motes(24, 5)}
    <div style="position:absolute;left:46px;top:46px;width:420px;height:420px;border-radius:50%;
                background:repeating-conic-gradient(#f7a8c0 0 15deg,#fff 15deg 30deg);
                box-shadow:0 0 0 6px #fff,0 0 0 10px #5fbfa3,0 24px 50px rgba(107,63,58,.3)"></div>
    <div style="position:absolute;left:74px;top:74px;width:364px;height:364px;border-radius:50%;background:radial-gradient(circle at 50% 45%,#fffdfb,#fdeff3)"></div>
    <div style="position:absolute;left:74px;top:74px;width:364px;height:364px;display:flex;align-items:center;justify-content:center">
      ${sprite('pink_vanilla_cupcake', 240)}</div>
    ${[[256, 34], [478, 256], [256, 478], [34, 256]].map(([x, y], i) => `<div style="position:absolute;left:${x - 20}px;top:${y - 20}px">${heartSvg(['#ef5f8b', '#9b7ce0', '#f59e6b', '#5fbfa3'][i], 40)}</div>`).join('')}`),

  'card-treats': () => card({
    kicker: '24 treats', title: 'Pastel<br>pastries',
    points: ['Cakes, cupcakes, macarons, cookies and pies', 'Cinnamon rolls, mini donuts and peach pastries',
      'Heart candy, lollipops and marshmallow clouds', 'Treats with more steps fill you up more', 'Many give a small, short bonus effect'],
    art: grid(4, TREATS.map((id) => tile(sprite(id, 96), nameOf(id), { art: 104, size: 17 })), 12) +
      stats([['Treats', '24'], ['Drinks', '8'], ['Bonus effects', '11'], ['Recipes', '64']]) }),

  'card-drinks': () => card({
    kicker: 'Teas, milk and shakes', title: 'Sip<br>something',
    points: ['Eight drinks with the drinking animation', 'You can always sip one, even when full', 'They hand back their glass bottle or teacup',
      'Teas, lemonade and cocoa give small bonuses', 'Every effect can be turned off or rescaled'],
    art: grid(4, DRINKS.map(([id, sub]) => tile(sprite(id, 128), nameOf(id), { art: 140, size: 21, sub })), 16) }),

  'card-oven': () => card({
    kicker: 'Bakery Oven', title: 'Bake it<br>golden',
    points: ['4 ingredient slots and any furnace fuel', 'Glows, crackles and puffs smoke while it bakes', 'Rings a bell and pops hearts when it’s done',
      'Gives experience like a furnace', 'Hoppers: ingredients on top, fuel on the sides'],
    art: `<div style="display:flex;gap:20px;align-items:center">
      <div style="flex:none;width:250px;display:flex;flex-direction:column;gap:16px">
        ${tile(model('bakery_oven', 170), 'Bakery Oven', { art: 190, size: 22, sub: 'Idle' })}
        ${tile(model('bakery_oven_on', 170), 'Bakery Oven', { art: 190, size: 22, sub: 'Baking, light 13' })}</div>
      <div style="flex:1">${framed(region('gui-oven.jpg', OVEN.x, OVEN.y, OVEN.w, OVEN.h, 710))}</div></div>` }),

  'card-mixing': () => card({
    kicker: 'Mixing Bowl', title: 'Stir it<br>up',
    points: ['6 ingredient slots and a slot for bottles', 'Needs no fuel at all', 'Ingredients swirl inside the bowl as they mix',
      'Batters, doughs, icings and every cold drink', 'Two of one ingredient can come from one stack'],
    art: `<div style="display:flex;gap:20px;align-items:center">
      <div style="flex:none;width:250px;display:flex;flex-direction:column;gap:16px">
        ${tile(model('mixing_bowl', 120), 'Mixing Bowl', { art: 150, size: 22, sub: 'No fuel needed' })}
        ${tile(`${sprite('cake_batter', 64)}${sprite('pink_icing', 64)}${sprite('tapioca_pearls', 64)}`, 'Batters & icings', { art: 90, size: 20 })}</div>
      <div style="flex:1">${framed(region('gui-mixing-bowl.jpg', BOWL.x, BOWL.y, BOWL.w, BOWL.h, 710))}</div></div>` }),

  'card-decorating': () => card({
    kicker: 'Decoration Station', title: 'Finish it<br>pretty',
    points: ['A base treat plus up to two toppings', 'The decorated treat is ready at once', 'A sparkly chime when you take it out',
      'Honey bottles hand back their glass bottle', 'Icing and sugar flowers come from the bowl'],
    art: `<div style="display:flex;flex-direction:column;gap:12px">
      ${recipeRow('plain_cupcake', ['pink_icing', 'sugar_flowers'], 'pink_vanilla_cupcake')}
      ${recipeRow('sponge_cake', ['cream', 'strawberry'], 'strawberry_cake')}
      ${recipeRow('macaron_shells', ['icing', 'lavender'], 'lavender_macaron', 2)}
      ${recipeRow('sugar_cookie', ['icing', 'sprinkles'], 'rainbow_cookie')}
      ${framed(region('gui-decoration-station.jpg', STATION.x, STATION.y, STATION.w, STATION.h, 980))}</div>` }),

  'card-book': () => card({
    kicker: 'Sweet Treats Recipe Book', title: 'Discover<br>every recipe',
    points: ['Tabs for the oven, bowl, decorating and crafting', 'Ingredients, station, time, food and bonus', 'Unknown recipes stay as dark silhouettes',
      'Make or pick up a treat to discover it', 'A found / total counter on every tab'],
    art: pair(region('book-oven.jpg', BOOK.x, BOOK.y, BOOK.w, BOOK.h, 626), region('book-decorating.jpg', BOOKD.x, BOOKD.y, BOOKD.w, BOOKD.h, 626), 'Bakery Oven', 'Decorating', 780, '64%') }),

  'card-garden': () => card({
    kicker: 'Crops and fruit trees', title: 'Grow your<br>own',
    points: ['Strawberries, vanilla and lavender on farmland', 'Peach and cherry trees grow from pits', 'Pick ripe fruit and the tree grows it again',
      'Wild patches in plains, meadows and jungles', 'Grass, ferns and cherry leaves drop extras'],
    art: `<div style="display:flex;flex-direction:column;gap:14px">${grid(3, [
      ['strawberry_crop_stage3', 'strawberry', 'Strawberries'], ['vanilla_crop_stage3', 'vanilla_pod', 'Vanilla'], ['lavender_crop_stage3', 'lavender', 'Lavender']]
      .map(([m, item, t]) => tile(`${model(m, 150)}<div style="margin-left:-20px">${sprite(item, 64)}</div>`, t, { art: 170, size: 22, sub: '8 growth stages' })), 14)}
      <div class="tile" style="display:flex;align-items:flex-end;justify-content:space-around;padding:14px 20px 12px">
        ${['peach_tree_stage0', 'peach_tree_stage1', 'peach_tree_stage2', 'peach_tree_stage3'].map((m, i) => `<div style="text-align:center">${model(m, 150)}
          <div class="s" style="font-size:15px;margin-top:4px">${['Sapling', 'Young', 'Blossoming', 'Fruiting'][i]}</div></div>`).join('')}
        <div style="text-align:center">${model('cherry_tree_stage3', 150)}<div class="s" style="font-size:15px;margin-top:4px">Cherry tree</div></div></div></div>` }),

  'card-displays': () => card({
    kicker: 'Bakery decorations', title: 'Open your<br>shop',
    points: ['Cake displays, counters, stands, plates and jars', 'Right-click a treat on, empty hand to take it', 'Glass jars hold a whole stack of one treat',
      'Strawberry cake and cherry pie, eaten by slice', 'A tea set, a shop sign and a menu board'],
    art: framed(region('counter.jpg', 0, 0, 1920, 1080, 978)) + `<div style="margin-top:18px">${grid(6, [
      ['cake_display', 'Cake Display'], ['cupcake_stand', 'Cupcake Stand'], ['glass_jar', 'Glass Jar'], ['bakery_counter', 'Counter'],
      ['tea_set', 'Tea Set'], ['decorative_menu', 'Menu Board']].map(([m, t]) => tile(model(m, 80), t, { art: 90, size: 17 })), 10)}</div>` }),

  'card-config': () => card({
    kicker: 'Make it yours', title: 'Set the<br>rules',
    points: ['Turn treat effects off or scale their length', 'Speed up the oven and the bowl', 'A fuel-free oven if you want one',
      'Hide undiscovered ingredients for a challenge', 'Extra drops, tree growth and trader trades'],
    art: `<div style="display:flex;flex-direction:column;gap:20px">
      ${code('config/sweettreats-common.toml', `${k('[food]')}
  ${k('foodEffects')} = ${v('true')}
  ${k('effectDurationMultiplier')} = ${v('1.0')}
${k('[machines]')}
  ${k('ovenSpeedMultiplier')} = ${v('1.0')}
  ${k('ovenRequiresFuel')} = ${v('true')}
${k('[recipe_book]')}
  ${k('hideUndiscoveredIngredients')} = ${v('false')}
${k('[world]')}
  ${k('fruitTreeGrowthChance')} = ${v('5')}  ${cm('# 1 in N per random tick')}`)}
      ${code('config/sweettreats-client.toml', `${k('[visuals]')}
  ${k('cookingParticles')} = ${v('true')}
  ${k('cookingAnimations')} = ${v('true')}`)}</div>` }),
};

const headers = { 'header-treats': 'The Treats', 'header-drinks': 'Drinks & Bonuses', 'header-oven': 'Bakery Oven',
  'header-mixing': 'Mixing Bowl', 'header-decorating': 'Decoration Station', 'header-book': 'The Recipe Book',
  'header-garden': 'Crops & Fruit Trees', 'header-displays': 'Bakery Decorations', 'header-recipes': 'Getting Started',
  'header-config': 'Configuration', 'header-everything': 'Everything Else', 'header-compatibility': 'Downloads & Compatibility',
  'header-faq': 'Questions', 'header-gallery': 'Gallery' };
for (const [name, title] of Object.entries(headers)) assets[name] = () => header(title);
assets.divider = () => page(1400, 40, `<div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:14px">
  <div class="rule" style="width:480px;-webkit-mask-image:linear-gradient(90deg,transparent,#000);mask-image:linear-gradient(90deg,transparent,#000)"></div>
  ${heartSvg('#9b7ce0', 22)}${heartSvg('#ef5f8b', 28)}${heartSvg('#5fbfa3', 22)}
  <div class="rule" style="width:480px;-webkit-mask-image:linear-gradient(90deg,#000,transparent);mask-image:linear-gradient(90deg,#000,transparent)"></div></div>`, true);

(async () => {
  const wanted = process.argv.slice(2);
  const browser = await chromium.launch(process.env.CHROMIUM
    ? { executablePath: process.env.CHROMIUM, args: ['--no-sandbox', '--use-angle=swiftshader', '--enable-unsafe-swiftshader'] }
    : {});
  fs.mkdirSync(out, { recursive: true });
  for (const [name, build] of Object.entries(assets)) {
    if (wanted.length && !wanted.includes(name)) continue;
    const html = build();
    const size = html.match(/body \{ width: (\d+)px; height: (\d+)px; \}/);
    const tab = await browser.newPage({ viewport: { width: +size[1], height: +size[2] }, deviceScaleFactor: 1 });
    const file = path.join(__dirname, `.render-${name}.html`);
    fs.writeFileSync(file, html);
    await tab.goto(url(file));
    await tab.evaluate(() => document.fonts.ready);
    await tab.waitForLoadState('networkidle');
    await tab.waitForFunction(() => document.body.dataset.items === 'ready');
    await tab.screenshot({ path: path.join(out, `${name}.png`), omitBackground: name === 'divider' || name.startsWith('header-') });
    fs.unlinkSync(file);
    await tab.close();
    console.log('rendered', name);
  }
  await browser.close();
})().catch((error) => { console.error(error); process.exit(1); });
