var Me={radius:500,softness:.75,size:1,scatter:25,drift:1,aberration:40,bend:50,fade:.85,threshold:.1,background:"#000000",smoothing:.25},Pe=`#version 300 es
precision highp float;
layout(location = 0) in vec2 aPos;
out vec2 vUv;
void main () {
  vUv = aPos * 0.5 + 0.5;
  gl_Position = vec4(aPos, 0.0, 1.0);
}`,Ae=`#version 300 es
precision highp float;
in vec2 vUv;
out vec4 outColor;
uniform sampler2D uContent;
uniform vec2 uRes;
uniform float uDpr;
uniform vec2 uPointer;
uniform float uActive;
uniform float uRadius;
uniform float uSoftness;
uniform float uSize;
uniform float uScatter;
uniform float uDrift;
uniform float uAberration;
uniform float uBend;
uniform float uFade;
uniform float uThreshold;
uniform vec3 uBg;
uniform float uTime;
uniform float uMaxX;
uniform float uCrisp;

float hash (vec2 p) {
  vec3 p3 = fract(vec3(p.xyx) * 0.1031);
  p3 += dot(p3, p3.yzx + 33.33);
  return fract((p3.x + p3.y) * p3.z);
}

vec4 samp (vec2 p) {
  vec2 uv = p / uRes;
  uv = clamp(uv, vec2(0.001), vec2(uMaxX - 0.001, 0.999));
  return texture(uContent, uv);
}

void main () {
  vec2 pc = vec2(vUv.x, 1.0 - vUv.y) * uRes;
  if (pc.x > uMaxX * uRes.x) {
    outColor = vec4(0.0);
    return;
  }
  if (uCrisp > 0.5) {
    outColor = samp(pc);
    return;
  }

  float dist = length(pc - uPointer);
  float radius = max(uRadius, 1.0);
  float inner = radius * (1.0 - clamp(uSoftness, 0.02, 1.0));
  float e = (1.0 - smoothstep(inner, radius, dist)) * uActive;

  float band = radius * 0.9;
  float ring = smoothstep(inner, radius, dist)
    * (1.0 - smoothstep(radius, radius + band, dist))
    * uActive;

  vec2 dir = (pc - uPointer) / max(dist, 1e-3);
  vec2 tang = vec2(-dir.y, dir.x);
  vec2 warp = (dir * -1.0 + tang * 0.6) * uBend * ring;
  float ca = uAberration * ring;

  float cellPx = max(uSize, 0.5) * uDpr;
  vec2 cell = floor(gl_FragCoord.xy / cellPx);
  float n1 = hash(cell);
  float n2 = hash(cell + vec2(3.1, 7.7));
  float n3 = hash(cell + vec2(9.3, 1.3));
  float ft = floor(uTime * (2.0 + uDrift * 6.0));
  float n4 = hash(cell + vec2(ft * 0.613, ft * 0.831));

  float g0 = uThreshold * 0.6;
  float g1 = uThreshold * 1.6 + 0.01;
  vec3 lw = vec3(0.299, 0.587, 0.114);

  vec2 bp = pc + warp;
  vec4 bR = samp(bp + dir * ca);
  vec4 bC = samp(bp);
  vec4 bB = samp(bp - dir * ca);
  vec3 baseRgb = vec3(bR.r, bC.g, bB.b);
  float uiHome = smoothstep(g0, g1, dot(abs(baseRgb - uBg), lw));

  float rad = uScatter * pow(n1, 2.5) * (1.0 - e);
  float ang = n2 * 6.2832 + uTime * uDrift * (0.5 + n3 * 1.5);
  vec2 dustP = bp + vec2(cos(ang), sin(ang)) * rad;

  vec4 dR = samp(dustP + dir * ca);
  vec4 dC = samp(dustP);
  vec4 dB = samp(dustP - dir * ca);
  vec3 dustRgb = vec3(dR.r, dC.g, dB.b);
  float lumD = dot(dustRgb, lw);
  float dDust = dot(abs(dustRgb - uBg), lw);

  float gate = smoothstep(g0, g1, dDust);
  float falloff = 1.0 - 0.7 * rad / max(uScatter, 1.0);
  float prob = clamp(gate * (0.15 + 1.2 * sqrt(dDust)) * falloff, 0.0, 1.0) * uiHome;
  float speck = step(n4 * 0.999, prob);

  float shade = pow(lumD, 0.4) * (0.8 + 0.4 * n3);
  vec3 dustCol = mix(uBg, vec3(shade), clamp(uFade, 0.0, 1.0));

  vec3 unrevealed = mix(mix(baseRgb, uBg, uiHome), dustCol, speck);
  vec3 col = mix(unrevealed, baseRgb, e);
  float alpha = mix(bC.a, dC.a, speck * (1.0 - e));
  outColor = vec4(col, alpha);
}`,oe=null;function Se(d){if(typeof document>"u")return[0,0,0];if(!oe){let t=document.createElement("canvas");t.width=1,t.height=1,oe=t.getContext("2d",{willReadFrequently:!0})}if(!oe)return[0,0,0];oe.fillStyle="#000000",oe.fillStyle=d,oe.clearRect(0,0,1,1),oe.fillRect(0,0,1,1);let f=oe.getImageData(0,0,1,1).data;return[f[0]/255,f[1]/255,f[2]/255]}function de(){if(typeof document>"u")return!1;let d=document.createElement("canvas"),f=d.getContext("2d");return!!(f&&typeof f.drawElementImage=="function"&&typeof d.requestPaint=="function")}function he(d,f={}){let t={...Me,...f},{source:c,content:E,output:n}=d,e=n.getContext("webgl2",{alpha:!0,depth:!1,stencil:!1,antialias:!1,premultipliedAlpha:!1});if(!e||e.isContextLost())return null;let D=c.getContext("2d"),L=c,M=!!(D&&typeof D.drawElementImage=="function"&&typeof L.requestPaint=="function"),U=!1,ee=()=>{};M&&(L.onpaint=()=>{try{D.reset(),D.drawElementImage(E,0,0),U=!0,ee()}catch{}});function te(s,p){let v=e.createShader(s);return e.shaderSource(v,p),e.compileShader(v),e.getShaderParameter(v,e.COMPILE_STATUS)||console.error("ParticleReveal shader error:",e.getShaderInfoLog(v)),v}let ne=te(e.VERTEX_SHADER,Pe),g=te(e.FRAGMENT_SHADER,Ae),m=e.createProgram();e.attachShader(m,ne),e.attachShader(m,g),e.linkProgram(m);let o={},ae=e.getProgramParameter(m,e.ACTIVE_UNIFORMS);for(let s=0;s<ae;s++){let p=e.getActiveUniform(m,s);o[p.name]=e.getUniformLocation(m,p.name)}let H=e.createBuffer();e.bindBuffer(e.ARRAY_BUFFER,H),e.bufferData(e.ARRAY_BUFFER,new Float32Array([-1,-1,1,-1,-1,1,1,1]),e.STATIC_DRAW),e.enableVertexAttribArray(0),e.vertexAttribPointer(0,2,e.FLOAT,!1,0,0);let F=e.createTexture();e.bindTexture(e.TEXTURE_2D,F),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_MIN_FILTER,e.LINEAR),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_MAG_FILTER,e.LINEAR),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_WRAP_S,e.CLAMP_TO_EDGE),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_WRAP_T,e.CLAMP_TO_EDGE),e.texImage2D(e.TEXTURE_2D,0,e.RGBA,1,1,0,e.RGBA,e.UNSIGNED_BYTE,new Uint8Array([0,0,0,0]));let re=1;function S(){let s=Math.min(window.devicePixelRatio||1,2),p=Math.max(1,Math.round(n.clientWidth*s)),v=Math.max(1,Math.round(n.clientHeight*s));if((n.width!==p||n.height!==v)&&(n.width=p,n.height=v),re=Math.min(1,Math.max(.05,E.clientWidth/Math.max(n.clientWidth,1))),M){let A=Math.max(1,Math.round(c.clientWidth)),Z=Math.max(1,Math.round(c.clientHeight));(c.width!==A*s||c.height!==Z*s)&&(c.width=A*s,c.height=Z*s),L.requestPaint()}}let r={x:-1e5,y:-1e5,tx:-1e5,ty:-1e5,active:0,target:0},I=0,_="",y=[0,0,0],B=window.matchMedia("(prefers-reduced-motion: reduce)"),X=B.matches;S();function J(){!M||!U||(U=!1,e.bindTexture(e.TEXTURE_2D,F),e.texImage2D(e.TEXTURE_2D,0,e.RGBA,e.RGBA,e.UNSIGNED_BYTE,c))}function T(){J();let s=Math.max(n.clientWidth,1),p=Math.max(n.clientHeight,1),v=n.width/s;e.useProgram(m),e.activeTexture(e.TEXTURE0),e.bindTexture(e.TEXTURE_2D,F),e.uniform1i(o.uContent,0),e.uniform2f(o.uRes,s,p),e.uniform1f(o.uDpr,v),e.uniform2f(o.uPointer,r.x,r.y),e.uniform1f(o.uActive,r.active),e.uniform1f(o.uRadius,Math.max(t.radius,1)),e.uniform1f(o.uSoftness,t.softness),e.uniform1f(o.uSize,Math.max(t.size,.5)),e.uniform1f(o.uScatter,Math.max(t.scatter,0)),e.uniform1f(o.uDrift,Math.max(t.drift,0)),e.uniform1f(o.uAberration,Math.max(t.aberration,0)),e.uniform1f(o.uBend,Math.max(t.bend,0)),e.uniform1f(o.uFade,t.fade),e.uniform1f(o.uThreshold,Math.max(t.threshold,0)),t.background!==_&&(_=t.background,y=Se(t.background)),e.uniform3f(o.uBg,y[0],y[1],y[2]),e.uniform1f(o.uTime,I),e.uniform1f(o.uMaxX,re),e.uniform1f(o.uCrisp,X||!M?1:0),e.bindFramebuffer(e.FRAMEBUFFER,null),e.viewport(0,0,n.width,n.height),e.drawArrays(e.TRIANGLE_STRIP,0,4)}let G=0,q=performance.now(),Y=!1,Q=!1,j=!0;function V(s){if(Y)return;if(!j){Q=!1;return}let p=Math.min((s-q)/1e3,1/30);q=s,I+=p;let v=Math.max(t.smoothing,1e-4),A=X?1:1-Math.exp(-p/v);if(r.x+=(r.tx-r.x)*A,r.y+=(r.ty-r.y)*A,r.active+=(r.target-r.active)*A,T(),Math.abs(r.tx-r.x)<.1&&Math.abs(r.ty-r.y)<.1&&Math.abs(r.target-r.active)<.001&&!U&&(X||!M||t.drift<=0)){r.x=r.tx,r.y=r.ty,r.active=r.target,Q=!1;return}G=requestAnimationFrame(V)}function P(){Y||Q||!j||(Q=!0,q=performance.now(),G=requestAnimationFrame(V))}ee=P,P();function k(){X=B.matches,P()}B.addEventListener("change",k);let z=new ResizeObserver(()=>{S(),P()});z.observe(n),z.observe(E);let K=new IntersectionObserver(s=>{j=s[s.length-1]?.isIntersecting??!0,j&&P()});K.observe(n);let W=n.parentElement??n;function $(s){let p=n.getBoundingClientRect(),v=s.clientX-p.left,A=s.clientY-p.top;r.target===0&&r.active<.001&&(r.x=v,r.y=A),r.tx=v,r.ty=A,r.target=1,P()}function C(){r.target=0,P()}return W.addEventListener("pointermove",$),W.addEventListener("pointerleave",C),{setOptions(s){Object.entries(s).some(([p,v])=>t[p]!==v)&&(Object.assign(t,s),P())},resize(){S(),P()},destroy(){Y=!0,cancelAnimationFrame(G),z.disconnect(),K.disconnect(),B.removeEventListener("change",k),W.removeEventListener("pointermove",$),W.removeEventListener("pointerleave",C),e.deleteTexture(F),e.deleteProgram(m),e.deleteShader(ne),e.deleteShader(g),e.deleteBuffer(H),M&&(L.onpaint=null)}}}var Ce={shape:"circle",size:120,aspect:1.7,corner:32,ior:1.5,edge:.7,bevel:4,depth:250,aberration:1,blur:0,reflection:1,shine:.01,zoom:1.5,targets:"[data-glass-target]",follow:.2},Ie=`#version 300 es
precision highp float;
layout(location = 0) in vec2 aPos;
void main () {
  gl_Position = vec4(aPos, 0.0, 1.0);
}`,_e=`#version 300 es
precision highp float;
out vec4 outColor;
uniform sampler2D uContent;
uniform vec2 uResolution;
uniform float uMaxX;
uniform float uHasContent;
uniform vec2 uCenter;
uniform vec2 uHalf;
uniform float uCorner;
uniform float uEdge;
uniform float uBevel;
uniform float uIor;
uniform float uDepth;
uniform float uAberration;
uniform float uBlur;
uniform float uReflect;
uniform float uShine;
uniform float uZoom;
uniform float uAlpha;

const float PI = 3.14159265358979;
const float AIR_IOR = 1.0003;
const vec3 INCIDENT = vec3(0.0, 0.0, 1.0);

float pow2 (float x) { return x * x; }
float pow5 (float x) { float x2 = x * x; return x2 * x2 * x; }
float linearStep (float e0, float e1, float x) {
  return clamp((x - e0) / (e1 - e0), 0.0, 1.0);
}

float sdf (vec2 p) {
  vec2 q = abs(p) - (uHalf - vec2(uCorner));
  return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - uCorner;
}

float ign (vec2 v) {
  return fract(52.9829189 * fract(0.06711056 * v.x + 0.00583715 * v.y));
}

vec3 page (vec2 px, float lod) {
  vec2 uv = px / uResolution;
  uv.x = clamp(uv.x, 0.0005, uMaxX - 0.0005);
  uv.y = clamp(uv.y, 0.0005, 0.9995);
  return pow(textureLod(uContent, vec2(uv.x, 1.0 - uv.y), lod).rgb, vec3(2.2));
}

float iorForWavelength (float wavelength) {
  float ab = uAberration * 0.1;
  return mix(uIor + ab, uIor - ab,
    1.0 - pow(1.0 - linearStep(450.0, 650.0, wavelength), 4.0));
}

vec3 pageAA (vec2 px, float minLod) {
  float footprint = max(length(fwidth(px)), 1.0);
  return page(px, max(minLod, log2(footprint)));
}

vec3 sampleRefraction (vec2 basePx, float rim, vec3 normal, float glassIor) {
  vec3 rv = refract(INCIDENT, normal, AIR_IOR / glassIor);
  rv /= abs(rv.z) / uDepth;

  return pageAA(basePx + rv.xy, uBlur * (1.0 + rim));
}

float fresnelSchlick (float cosTheta, float f0) {
  return f0 + (1.0 - f0) * pow5(1.0 - cosTheta);
}

float smithSchlickDenom (float cosTheta, float k) {
  return cosTheta * (1.0 - k) + k;
}

float ggx (float roughness, float NDotL, float NDotV, float NDotH) {
  if (NDotL <= 0.0) return 0.0;
  float a2 = pow2(roughness);
  float d = a2 / (PI * pow2(pow2(NDotH) * (a2 - 1.0) + 1.0));
  float k = roughness * 0.5;
  float v = 1.0 / (smithSchlickDenom(NDotL, k)
    * smithSchlickDenom(clamp(NDotV, 0.0, 1.0), k));
  return NDotL * d * v;
}

void main () {
  vec2 fragPx = gl_FragCoord.xy;
  vec2 p = fragPx - uCenter;
  float sd = sdf(p);

  float aa = 1.5;
  float mask = 1.0 - smoothstep(-aa, 0.0, sd);
  float alpha = mask * uAlpha
    * (1.0 - step(uMaxX, fragPx.x / uResolution.x));

  float minHalf = min(uHalf.x, uHalf.y);
  float edgeW = max(minHalf * (1.0 - clamp(uEdge, 0.0, 0.98)), 1.0);
  float rim = pow(linearStep(-edgeW, 0.0, sd), uBevel);

  float scatter = min(uBlur, 1.0) * 0.02;
  float randAngle = ign(fragPx) * PI * 2.0;
  vec3 flatNormal = normalize(
    vec3(sin(randAngle) * scatter, cos(randAngle) * scatter, -1.0));
  float e = 1.0;
  vec2 grad = vec2(
    sdf(p + vec2(e, 0.0)) - sdf(p - vec2(e, 0.0)),
    sdf(p + vec2(0.0, e)) - sdf(p - vec2(0.0, e)));
  vec3 rimNormal = vec3(normalize(grad + vec2(1e-5)), 0.0);
  vec3 normal = normalize(mix(flatNormal, rimNormal, rim));

  if (uHasContent < 0.5) {
    float ldot = dot(rimNormal.xy, normalize(vec2(-0.6, 0.8)));
    float band = pow(rim, 1.8);
    float arcs = pow(abs(ldot), 3.0) * (ldot > 0.0 ? 0.5 : 0.28);
    float shine = band * (0.04 + arcs) * max(uShine, 0.5);
    float a = alpha * clamp(0.06 + 0.12 * rim, 0.0, 1.0);
    outColor = vec4(vec3(shine * 1.6) * alpha, a);
    return;
  }

  vec2 basePx = uCenter + p / uZoom;

  vec3 refracted;
  if (uAberration > 0.001) {
    refracted = sampleRefraction(basePx, rim, normal, iorForWavelength(611.4))
      * vec3(1.0, 0.0, 0.0);
    refracted += sampleRefraction(basePx, rim, normal, iorForWavelength(570.5))
      * vec3(1.0, 1.0, 0.0);
    refracted += sampleRefraction(basePx, rim, normal, iorForWavelength(549.1))
      * vec3(0.0, 1.0, 0.0);
    refracted += sampleRefraction(basePx, rim, normal, iorForWavelength(491.4))
      * vec3(0.0, 1.0, 1.0);
    refracted += sampleRefraction(basePx, rim, normal, iorForWavelength(464.2))
      * vec3(0.0, 0.0, 1.0);
    refracted += sampleRefraction(basePx, rim, normal, iorForWavelength(374.0))
      * vec3(1.0, 0.0, 1.0);
    refracted /= 3.0;
  } else {
    refracted = sampleRefraction(basePx, rim, normal, uIor);
  }

  vec3 glass = refracted;
  if (uReflect > 0.001) {
    const vec3 V = vec3(0.0, 0.0, -1.0);
    float NDotV = clamp(dot(V, normal), 0.0, 1.0);
    float f0 = pow2((uIor - AIR_IOR) / (uIor + AIR_IOR));
    float fresnelV = fresnelSchlick(NDotV, f0) * uReflect;

    vec3 reflectVector = reflect(INCIDENT, normal);
    vec3 L = reflectVector;
    vec3 H = normalize(L + V);
    reflectVector /= abs(reflectVector.z) / uDepth;
    vec3 reflected = page(basePx + reflectVector.xy, 2.5 + uBlur);
    reflected *= ggx(0.5, dot(normal, L), NDotV, dot(normal, H));
    glass = mix(refracted, reflected, clamp(fresnelV, 0.0, 1.0));
  }

  if (uShine > 0.001) {
    float ldot = dot(rimNormal.xy, normalize(vec2(-0.6, 0.8)));
    float band = pow(rim, 1.8);
    float arcs = pow(abs(ldot), 3.0) * (ldot > 0.0 ? 0.5 : 0.28);
    glass += band * (0.04 + arcs) * uShine;
  }

  outColor = vec4(pow(glass, vec3(1.0 / 2.2)) * alpha, alpha);
}`;function pe(){if(typeof document>"u")return!1;let d=document.createElement("canvas"),f=d.getContext("2d");return!!(f&&typeof f.drawElementImage=="function"&&typeof d.requestPaint=="function")}function ve(d,f={}){let t={...Ce,...f},{source:c,content:E,output:n}=d,e=n.getContext("webgl2",{alpha:!0,depth:!1,stencil:!1,antialias:!1,premultipliedAlpha:!0});if(!e||e.isContextLost())return null;let D=c.getContext("2d"),L=c,M=!!(D&&typeof D.drawElementImage=="function"&&typeof L.requestPaint=="function"),U=!1,ee=()=>{};M&&(L.onpaint=()=>{try{D.reset(),D.drawElementImage(E,0,0),U=!0,ee()}catch{}});function te(a,x){let b=e.createShader(a);return e.shaderSource(b,x),e.compileShader(b),e.getShaderParameter(b,e.COMPILE_STATUS)||console.error("Glass shader error:",e.getShaderInfoLog(b)),b}let ne=te(e.VERTEX_SHADER,Ie),g=te(e.FRAGMENT_SHADER,_e),m=e.createProgram();e.attachShader(m,ne),e.attachShader(m,g),e.linkProgram(m);let o={},ae=e.getProgramParameter(m,e.ACTIVE_UNIFORMS);for(let a=0;a<ae;a++){let x=e.getActiveUniform(m,a);o[x.name]=e.getUniformLocation(m,x.name)}let H=e.createBuffer();e.bindBuffer(e.ARRAY_BUFFER,H),e.bufferData(e.ARRAY_BUFFER,new Float32Array([-1,-1,1,-1,-1,1,1,1]),e.STATIC_DRAW),e.enableVertexAttribArray(0),e.vertexAttribPointer(0,2,e.FLOAT,!1,0,0);let F=e.createTexture();e.bindTexture(e.TEXTURE_2D,F),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_MIN_FILTER,e.LINEAR_MIPMAP_LINEAR),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_MAG_FILTER,e.LINEAR),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_WRAP_S,e.CLAMP_TO_EDGE),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_WRAP_T,e.CLAMP_TO_EDGE),e.texImage2D(e.TEXTURE_2D,0,e.RGBA,1,1,0,e.RGBA,e.UNSIGNED_BYTE,new Uint8Array([0,0,0,0])),e.generateMipmap(e.TEXTURE_2D);let re=1;function S(){let a=Math.min(window.devicePixelRatio||1,2),x=Math.max(1,Math.round(n.clientWidth*a)),b=Math.max(1,Math.round(n.clientHeight*a));if((n.width!==x||n.height!==b)&&(n.width=x,n.height=b),re=Math.min(1,Math.max(.05,E.clientWidth/Math.max(n.clientWidth,1))),M){let w=Math.max(1,Math.round(c.clientWidth)),N=Math.max(1,Math.round(c.clientHeight));(c.width!==w*a||c.height!==N*a)&&(c.width=w*a,c.height=N*a),L.requestPaint()}}S();function r(){!M||!U||(U=!1,e.bindTexture(e.TEXTURE_2D,F),e.texImage2D(e.TEXTURE_2D,0,e.RGBA,e.RGBA,e.UNSIGNED_BYTE,c),e.generateMipmap(e.TEXTURE_2D))}let I=n.clientWidth/2,_=n.clientHeight/2,y=0,B=0,X=I,J=_,T=1,G=1,q=!1;function Y(){let a=Math.max(t.size,8);return t.shape==="rectangle"?[a*Math.min(Math.max(t.aspect,1),4),a]:[a,a]}function Q(){r();let a=n.width/Math.max(n.clientWidth,1);if(e.bindFramebuffer(e.FRAMEBUFFER,null),e.viewport(0,0,n.width,n.height),e.disable(e.SCISSOR_TEST),e.clearColor(0,0,0,0),e.clear(e.COLOR_BUFFER_BIT),y<=.004)return;let[x,b]=Y(),w=x*y,N=b*y,se=Math.min(y*5,1),i=I*a,h=n.height-_*a,u=4*a,l=Math.max(0,Math.floor(i-w*a-u)),R=Math.max(0,Math.floor(h-N*a-u));e.enable(e.SCISSOR_TEST),e.scissor(l,R,Math.min(n.width-l,Math.ceil(w*a*2+u*2)),Math.min(n.height-R,Math.ceil(N*a*2+u*2))),e.useProgram(m),e.activeTexture(e.TEXTURE0),e.bindTexture(e.TEXTURE_2D,F),e.uniform1i(o.uContent,0),e.uniform2f(o.uResolution,n.width,n.height),e.uniform1f(o.uMaxX,re),e.uniform1f(o.uHasContent,M?1:0),e.uniform2f(o.uCenter,i,h),e.uniform2f(o.uHalf,w*a,N*a);let ie=t.shape==="circle"?Math.min(w,N):Math.min(Math.max(t.corner,0),Math.min(w,N));e.uniform1f(o.uCorner,ie*a),e.uniform1f(o.uEdge,Math.min(Math.max(t.edge,0),.98)),e.uniform1f(o.uBevel,Math.max(t.bevel,.5)),e.uniform1f(o.uIor,Math.min(Math.max(t.ior,1.01),2.5)),e.uniform1f(o.uDepth,Math.max(t.depth,0)*a),e.uniform1f(o.uAberration,Math.max(t.aberration,0)),e.uniform1f(o.uBlur,Math.max(t.blur,0)),e.uniform1f(o.uReflect,Math.max(t.reflection,0)),e.uniform1f(o.uShine,Math.max(t.shine,0)),e.uniform1f(o.uZoom,Math.max(T,1)),e.uniform1f(o.uAlpha,se),e.drawArrays(e.TRIANGLE_STRIP,0,4),e.disable(e.SCISSOR_TEST)}let j=0,V=performance.now(),P=!1,k=!1,z=!0,K=window.matchMedia("(prefers-reduced-motion: reduce)"),W=K.matches;function $(a){if(P)return;if(!z){k=!1;return}let x=Math.min((a-V)/1e3,1/30);V=a;let b=Math.min(Math.max(t.follow,.02),1),w=W||b>=1?1:1-Math.exp(-x*(4+b*26)),N=W?1:1-Math.exp(-x*7),se=W?1:1-Math.exp(-x*11);if(I+=(X-I)*w,_+=(J-_)*w,T+=(G-T)*N,y+=(B-y)*se,Q(),Math.abs(X-I)<.1&&Math.abs(J-_)<.1&&Math.abs(G-T)<.002&&Math.abs(B-y)<.002&&!U){I=X,_=J,T=G,y=B,k=!1;return}j=requestAnimationFrame($)}function C(){P||k||!z||(k=!0,V=performance.now(),j=requestAnimationFrame($))}ee=C,C();function s(a){let x=n.getBoundingClientRect();X=a.clientX-x.left,J=a.clientY-x.top,q||(I=X,_=J,q=!0),B=1;let b=a.target;G=t.zoom>1&&b?.closest?.(t.targets)?Math.min(Math.max(t.zoom,1),4):1,C()}function p(){B=0,G=1,q=!1,C()}E.addEventListener("pointermove",s,{passive:!0}),E.addEventListener("pointerleave",p,{passive:!0});function v(){C()}E.addEventListener("scroll",v,{passive:!0});function A(){W=K.matches,C()}K.addEventListener("change",A);let Z=new ResizeObserver(()=>{S(),C()});Z.observe(n),Z.observe(E);let O=new IntersectionObserver(a=>{z=a[a.length-1]?.isIntersecting??!0,z&&C()});return O.observe(n),{setOptions(a){Object.entries(a).some(([x,b])=>t[x]!==b)&&(Object.assign(t,a),C())},resize(){S(),C()},destroy(){P=!0,cancelAnimationFrame(j),E.removeEventListener("pointermove",s),E.removeEventListener("pointerleave",p),E.removeEventListener("scroll",v),Z.disconnect(),O.disconnect(),K.removeEventListener("change",A),e.deleteTexture(F),e.deleteProgram(m),e.deleteShader(ne),e.deleteShader(g),e.deleteBuffer(H),M&&(L.onpaint=null)}}}var we={tileSize:150,gap:0,cornerRadius:0,amplitude:2.5,waveSpeed:.5,frequency:12,waveWidth:.05,fadeTime:.2,maxLift:1,jitter:0,liftHeight:60,perspective:1200,tilt:1,shading:.05,tint:[0,.33,1],tintStrength:.1,idleRipples:0},ce=64,ye=.03,ge=3,De=`#version 300 es
precision highp float;
layout(location = 0) in vec2 aPos;
out vec2 vUv;
void main () {
  vUv = aPos * 0.5 + 0.5;
  gl_Position = vec4(aPos, 0.0, 1.0);
}`,Le=`#version 300 es
precision highp float;
out vec4 outColor;
uniform sampler2D uTrail;
uniform int uTrailCount;
uniform float uWorldPerTile;
uniform float uWaveSpeed;
uniform float uFrequency;
uniform float uWaveWidth;
uniform float uFadeTime;
uniform float uAmplitude;
uniform float uJitter;
uniform float uMaxLift;

vec2 hash2 (vec2 p) {
  p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
  return fract(sin(p) * 43758.5453123) - 0.5;
}

void main () {
  vec2 tile = floor(gl_FragCoord.xy);
  vec2 world = (tile + 0.5) * uWorldPerTile + hash2(tile) * uJitter * 0.12;

  float waveHeight = 0.0;
  float totalWeight = 0.0;

  for (int i = 0; i < 64; i++) {
    if (i >= uTrailCount) break;

    vec4 td = texelFetch(uTrail, ivec2(i, 0), 0);
    vec2 delta = world - td.xy;
    float dist = length(delta);
    float relDist = dist - uWaveSpeed * td.z;

    float window = exp(-(relDist * relDist) / (uWaveWidth * uWaveWidth));

    float fade = exp(-td.z / uFadeTime);
    float atten = 1.0 / (1.0 + dist * 3.0);
    float weight = fade * window * atten * td.w;
    waveHeight += weight * cos(uFrequency * relDist);
    totalWeight += weight;
  }

  float lift = clamp(
    waveHeight / max(totalWeight, 1.0) * uAmplitude, -uMaxLift, uMaxLift
  );

  outColor = vec4(lift * 0.5 + 0.5, 0.0, 0.0, 1.0);
}`,Ue=`#version 300 es
precision highp float;
in vec2 vUv;
out vec4 outColor;
uniform sampler2D uContent;
uniform sampler2D uTiles;
uniform vec2 uResolution;
uniform ivec2 uGridTiles;
uniform float uTilePx;
uniform float uGapPx;
uniform float uCornerPx;
uniform float uLiftPx;
uniform float uPersp;
uniform vec2 uVanish;
uniform float uShading;
uniform vec3 uTint;
uniform float uTintStrength;
uniform float uMaxX;
uniform float uHasContent;

float tileLift (ivec2 idx) {
  idx = clamp(idx, ivec2(0), uGridTiles - 1);
  return texelFetch(uTiles, idx, 0).r * 2.0 - 1.0;
}

float roundedBox (vec2 p, vec2 b, float r) {
  vec2 q = abs(p) - b + r;
  return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

float tileSd (vec2 w, ivec2 idx, float halfSize) {
  vec2 center = (vec2(idx) + 0.5) * uTilePx;
  return roundedBox(w - center, vec2(halfSize), min(uCornerPx, halfSize));
}

vec2 unproject (vec2 p, float z) {
  return uVanish + (p - uVanish) * (uPersp - z) / uPersp;
}

void main () {
  if (vUv.x > uMaxX) {
    outColor = vec4(0.0);
    return;
  }

  vec2 pos = vUv * uResolution;
  float halfSize = uTilePx * 0.5 - uGapPx * 0.5;

  float bestZ = -1e6;
  float edgeSd = 1.0;
  ivec2 bestIdx = ivec2(-1);
  vec2 bestW = pos;
  float bestLift = 0.0;
  bool bestIsWall = false;
  vec2 wallN = vec2(0.0);
  ivec2 lastIdx = ivec2(-9999);

  for (int k = 0; k < 8; k++) {
    float probeZ = (float(k) / 3.5 - 1.0) * uLiftPx;
    ivec2 idx = clamp(
      ivec2(floor(unproject(pos, probeZ) / uTilePx)),
      ivec2(0), uGridTiles - 1
    );
    if (all(equal(idx, lastIdx))) continue;
    lastIdx = idx;

    float lift = tileLift(idx);
    float h = lift * uLiftPx;

    if (h <= bestZ) continue;

    vec2 wh = unproject(pos, h);
    float sdTop = tileSd(wh, idx, halfSize);

    if (sdTop < 0.75) {
      bestZ = h;
      edgeSd = sdTop;
      bestIdx = idx;
      bestW = wh;
      bestLift = lift;
      bestIsWall = false;
    } else if (h > 0.0) {
      float sd0 = tileSd(pos, idx, halfSize);
      if (sd0 < 0.75) {
        float za = 0.0;
        float zb = h;
        for (int r = 0; r < 3; r++) {
          float zm = (za + zb) * 0.5;
          float sm = tileSd(unproject(pos, zm), idx, halfSize);
          if (sm < 0.0) { za = zm; } else { zb = zm; }
        }
        float zStar = (za + zb) * 0.5;
        if (zStar > bestZ) {
          vec2 wz = unproject(pos, zStar);
          vec2 e = vec2(0.75, 0.0);
          wallN = normalize(vec2(
            tileSd(wz + e.xy, idx, halfSize) - tileSd(wz - e.xy, idx, halfSize),
            tileSd(wz + e.yx, idx, halfSize) - tileSd(wz - e.yx, idx, halfSize)
          ) + 1e-5);
          bestZ = zStar;
          edgeSd = sd0;
          bestIdx = idx;
          bestW = wz;
          bestLift = lift;
          bestIsWall = true;
        }
      }
    }
  }

  if (bestIdx.x < 0) {
    outColor = vec4(0.0);
    return;
  }
  float mask = 1.0 - smoothstep(-0.75, 0.75, edgeSd);
  if (mask <= 0.0) {
    outColor = vec4(0.0);
    return;
  }

  vec2 tileOrigin = vec2(bestIdx) * uTilePx;
  vec2 samplePos = clamp(bestW, tileOrigin + 0.5, tileOrigin + uTilePx - 0.5);
  vec2 sampleUv = samplePos / uResolution;
  sampleUv.x = min(sampleUv.x, uMaxX - 0.002);
  vec4 content;
  if (uHasContent > 0.5) {
    content = texture(uContent, vec2(sampleUv.x, 1.0 - sampleUv.y));
  } else {
    float liftAmt = clamp(abs(bestLift), 0.0, 1.0);
    content = vec4(
      mix(vec3(0.62), uTint, clamp(uTintStrength, 0.0, 1.0)),
      liftAmt * 0.55);
  }

  float t = clamp(bestLift, 0.0, 1.0) * uTintStrength;
  vec3 col;
  float alpha;

  if (bestIsWall) {
    vec2 lightDir = normalize(vec2(-0.55, 0.8));
    float facing = dot(wallN, lightDir);
    float shade = 1.0 - (0.5 - 0.32 * facing) * uShading;
    col = content.rgb * shade;

    alpha = uHasContent > 0.5 ? max(content.a, 0.85) : min(content.a * 1.5, 0.85);
  } else {
    float gx = tileLift(bestIdx + ivec2(1, 0)) - tileLift(bestIdx - ivec2(1, 0));
    float gy = tileLift(bestIdx + ivec2(0, 1)) - tileLift(bestIdx - ivec2(0, 1));
    float shade = (gy - gx) * 0.25 * uShading;
    shade += clamp(bestLift, -1.0, 1.0) * 0.1 * uShading;
    col = content.rgb * (1.0 + shade * 0.85) + shade * 0.12;
    alpha = clamp(content.a + t + abs(shade) * 0.5, 0.0, 1.0);
  }

  col = mix(col, uTint, t);
  float aOut = alpha * mask;
  outColor = vec4(col * aOut, aOut);
}`;function xe(d,f={}){let t={...we,...f},{source:c,content:E,output:n}=d,e=n.getContext("webgl2",{alpha:!0,depth:!1,stencil:!1,antialias:!1,premultipliedAlpha:!0});if(!e||e.isContextLost())return null;let D=c.getContext("2d"),L=c,M=!!(D&&typeof D.drawElementImage=="function"&&typeof L.requestPaint=="function"),U=!1,ee=()=>{};M&&(L.onpaint=()=>{try{D.reset(),D.drawElementImage(E,0,0),U=!0,ee()}catch{}});function te(i,h){let u=e.createShader(i);return e.shaderSource(u,h),e.compileShader(u),e.getShaderParameter(u,e.COMPILE_STATUS)||console.error("Grid shader error:",e.getShaderInfoLog(u)),u}function ne(i){let h=te(e.VERTEX_SHADER,De),u=te(e.FRAGMENT_SHADER,i),l=e.createProgram();e.attachShader(l,h),e.attachShader(l,u),e.linkProgram(l);let R={},ie=e.getProgramParameter(l,e.ACTIVE_UNIFORMS);for(let le=0;le<ie;le++){let ue=e.getActiveUniform(l,le);R[ue.name]=e.getUniformLocation(l,ue.name)}return{program:l,uniforms:R,vertexShader:h,fragmentShader:u}}let g=ne(Ue),m=ne(Le),o=e.createBuffer();e.bindBuffer(e.ARRAY_BUFFER,o),e.bufferData(e.ARRAY_BUFFER,new Float32Array([-1,-1,1,-1,-1,1,1,1]),e.STATIC_DRAW),e.enableVertexAttribArray(0),e.vertexAttribPointer(0,2,e.FLOAT,!1,0,0);let ae=e.createTexture();e.bindTexture(e.TEXTURE_2D,ae),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_MIN_FILTER,e.LINEAR),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_MAG_FILTER,e.LINEAR),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_WRAP_S,e.CLAMP_TO_EDGE),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_WRAP_T,e.CLAMP_TO_EDGE),e.texImage2D(e.TEXTURE_2D,0,e.RGBA,1,1,0,e.RGBA,e.UNSIGNED_BYTE,new Uint8Array([0,0,0,0]));let H=new Float32Array(ce*4),F=e.createTexture();e.bindTexture(e.TEXTURE_2D,F),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_MIN_FILTER,e.NEAREST),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_MAG_FILTER,e.NEAREST),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_WRAP_S,e.CLAMP_TO_EDGE),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_WRAP_T,e.CLAMP_TO_EDGE),e.texImage2D(e.TEXTURE_2D,0,e.RGBA32F,ce,1,0,e.RGBA,e.FLOAT,H);function re(){return Math.min(window.devicePixelRatio||1,2)}let S=null,r=null,I=0,_=0;function y(){let i=Math.max(t.tileSize,8)*re(),h=Math.max(1,Math.ceil(n.width/i)),u=Math.max(1,Math.ceil(n.height/i));S&&h===I&&u===_||(I=h,_=u,S&&e.deleteTexture(S),r&&e.deleteFramebuffer(r),S=e.createTexture(),e.bindTexture(e.TEXTURE_2D,S),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_MIN_FILTER,e.NEAREST),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_MAG_FILTER,e.NEAREST),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_WRAP_S,e.CLAMP_TO_EDGE),e.texParameteri(e.TEXTURE_2D,e.TEXTURE_WRAP_T,e.CLAMP_TO_EDGE),e.texImage2D(e.TEXTURE_2D,0,e.RGBA,I,_,0,e.RGBA,e.UNSIGNED_BYTE,null),r=e.createFramebuffer(),e.bindFramebuffer(e.FRAMEBUFFER,r),e.framebufferTexture2D(e.FRAMEBUFFER,e.COLOR_ATTACHMENT0,e.TEXTURE_2D,S,0),e.bindFramebuffer(e.FRAMEBUFFER,null))}let B=1;function X(){let i=re(),h=Math.max(1,Math.round(n.clientWidth*i)),u=Math.max(1,Math.round(n.clientHeight*i));if((n.width!==h||n.height!==u)&&(n.width=h,n.height=u),B=Math.min(1,Math.max(.05,E.clientWidth/Math.max(n.clientWidth,1))),M){let l=Math.max(1,Math.round(c.clientWidth)),R=Math.max(1,Math.round(c.clientHeight));(c.width!==l*i||c.height!==R*i)&&(c.width=l*i,c.height=R*i),L.requestPaint()}}X();function J(){!M||!U||(U=!1,e.bindTexture(e.TEXTURE_2D,ae),e.texImage2D(e.TEXTURE_2D,0,e.RGBA,e.RGBA,e.UNSIGNED_BYTE,c))}let T=[],G=null,q=ge,Y=0;function Q(i){T.length>=ce&&T.shift(),T.push(i)}function j(i){let h=Math.max(t.fadeTime,.1)*4;for(let l=T.length-1;l>=0;l--)T[l].age+=i,T[l].age>h&&T.splice(l,1);if(q+=i,t.idleRipples>0&&q>=ge&&(Y+=i,Y>=t.idleRipples)){Y=0;let l=Math.max(n.clientWidth,1)/Math.max(n.clientHeight,1);Q({x:(.2+Math.random()*.6)*l,y:.2+Math.random()*.6,age:0,strength:.8+Math.random()*.3})}let u=Math.min(T.length,ce);for(let l=0;l<u;l++){let R=l*4;H[R]=T[l].x,H[R+1]=T[l].y,H[R+2]=T[l].age,H[R+3]=T[l].strength}return e.bindTexture(e.TEXTURE_2D,F),e.texSubImage2D(e.TEXTURE_2D,0,0,0,ce,1,e.RGBA,e.FLOAT,H),u}let V=.5,P=.5,k=.5,z=.5;function K(i,h){J(),y();let u=n.width/Math.max(n.clientWidth,1),l=Math.max(t.tileSize,8)*u,R=1-Math.exp(-h*4);V+=(k-V)*R,P+=(z-P)*R,e.useProgram(m.program),e.activeTexture(e.TEXTURE0),e.bindTexture(e.TEXTURE_2D,F),e.uniform1i(m.uniforms.uTrail,0),e.uniform1i(m.uniforms.uTrailCount,i),e.uniform1f(m.uniforms.uWorldPerTile,l/n.height),e.uniform1f(m.uniforms.uWaveSpeed,Math.max(t.waveSpeed,.01)),e.uniform1f(m.uniforms.uFrequency,t.frequency),e.uniform1f(m.uniforms.uWaveWidth,Math.max(t.waveWidth,.01)),e.uniform1f(m.uniforms.uFadeTime,Math.max(t.fadeTime,.1)),e.uniform1f(m.uniforms.uAmplitude,t.amplitude),e.uniform1f(m.uniforms.uJitter,t.jitter),e.uniform1f(m.uniforms.uMaxLift,Math.max(t.maxLift,.01)),e.bindFramebuffer(e.FRAMEBUFFER,r),e.viewport(0,0,I,_),e.drawArrays(e.TRIANGLE_STRIP,0,4),e.useProgram(g.program),e.activeTexture(e.TEXTURE0),e.bindTexture(e.TEXTURE_2D,ae),e.uniform1i(g.uniforms.uContent,0),e.uniform1f(g.uniforms.uHasContent,M?1:0),e.activeTexture(e.TEXTURE1),e.bindTexture(e.TEXTURE_2D,S),e.uniform1i(g.uniforms.uTiles,1),e.activeTexture(e.TEXTURE0),e.uniform2f(g.uniforms.uResolution,n.width,n.height),e.uniform2i(g.uniforms.uGridTiles,I,_),e.uniform1f(g.uniforms.uTilePx,l),e.uniform1f(g.uniforms.uGapPx,Math.max(t.gap,0)*u),e.uniform1f(g.uniforms.uCornerPx,Math.max(t.cornerRadius,0)*u),e.uniform1f(g.uniforms.uLiftPx,Math.max(t.liftHeight,0)*u),e.uniform1f(g.uniforms.uPersp,Math.max(t.perspective,100)*u),e.uniform2f(g.uniforms.uVanish,(.5+(V-.5)*t.tilt)*n.width,(.5+(.5-P)*t.tilt)*n.height),e.uniform1f(g.uniforms.uShading,t.shading),e.uniform3f(g.uniforms.uTint,t.tint[0],t.tint[1],t.tint[2]),e.uniform1f(g.uniforms.uTintStrength,t.tintStrength),e.uniform1f(g.uniforms.uMaxX,B),e.bindFramebuffer(e.FRAMEBUFFER,null),e.viewport(0,0,n.width,n.height),e.drawArrays(e.TRIANGLE_STRIP,0,4)}let W=0,$=performance.now(),C=!1,s=!1,p=!0,v=window.matchMedia("(prefers-reduced-motion: reduce)"),A=v.matches;function Z(i){if(C)return;if(!p){s=!1;return}let h=Math.min((i-$)/1e3,1/30);$=i;let u=A?0:j(h);K(u,h);let l=Math.abs(V-k)+Math.abs(P-z)>.001;if(!(!A&&(u>0||t.idleRipples>0||l))&&!U){s=!1;return}W=requestAnimationFrame(Z)}function O(){C||s||!p||(s=!0,$=performance.now(),W=requestAnimationFrame(Z))}ee=O,O();function a(){A=v.matches,A&&(T.length=0),O()}v.addEventListener("change",a);let x=new ResizeObserver(()=>{X(),O()});x.observe(n),x.observe(E);let b=new IntersectionObserver(i=>{p=i[i.length-1]?.isIntersecting??!0,p&&O()});b.observe(n);let w=n.parentElement??n;function N(i){if(A)return;let h=n.getBoundingClientRect(),u=Math.max(h.width,1)/Math.max(h.height,1),l=(i.clientX-h.left)/Math.max(h.width,1),R=(i.clientY-h.top)/Math.max(h.height,1);k=l,z=R;let ie=l*u,le=1-R,ue=.2;if(G){let be=ie-G.x,Re=le-G.y;if(ue=Math.hypot(be,Re),ue<ye){O();return}}Q({x:ie,y:le,age:0,strength:Math.min(Math.max(ue*6,.25),1.2)}),G={x:ie,y:le},q=0,Y=0,O()}function se(){k=.5,z=.5,O()}return w.addEventListener("pointermove",N),w.addEventListener("pointerleave",se),{setOptions(i){Object.entries(i).some(([h,u])=>t[h]!==u)&&(Object.assign(t,i),O())},resize(){X(),O()},destroy(){C=!0,cancelAnimationFrame(W),x.disconnect(),b.disconnect(),v.removeEventListener("change",a),w.removeEventListener("pointermove",N),w.removeEventListener("pointerleave",se),e.deleteTexture(ae),e.deleteTexture(F),S&&e.deleteTexture(S),r&&e.deleteFramebuffer(r);for(let i of[g,m])e.deleteProgram(i.program),e.deleteShader(i.vertexShader),e.deleteShader(i.fragmentShader);e.deleteBuffer(o),M&&(L.onpaint=null)}}}var Ee=window.matchMedia&&window.matchMedia("(prefers-reduced-motion: reduce)").matches;function me(d,f){try{f()}catch(t){console.warn("[crawler canvasui] "+d,t)}}function Fe(){let d=document.getElementById("prStage"),f=document.getElementById("prSource"),t=document.getElementById("prContent"),c=document.getElementById("prOutput");if(!d||!f||!t||!c)return;if(!de()||Ee){f.remove(),c.remove();return}d.classList.add("cu-pr-active");let E=t.getBoundingClientRect();f.appendChild(t),f.style.width=Math.max(1,Math.round(E.width))+"px",f.style.height=Math.max(1,Math.round(E.height))+"px",me("ParticleReveal",()=>{he({source:f,content:t,output:c},{radius:110,softness:.8,size:1.4,scatter:12,drift:.45,aberration:10,bend:14,fade:.9,background:"#0a0a0a",smoothing:.2})})}var fe=null;function Xe(){let d=document.getElementById("glassHost"),f=document.getElementById("glassOutput");if(!d||!f)return;if(!pe()||Ee){f.remove();return}let t=document.createElement("canvas");t.setAttribute("layoutsubtree",""),t.className="glass-source",d.insertBefore(t,d.firstChild),d.classList.add("cu-glass-active"),me("Glass",()=>{fe=ve({source:t,content:d,output:f},{shape:"rectangle",size:460,aspect:2.2,corner:22,ior:1.2,edge:.8,bevel:2,depth:16,aberration:.4,blur:.4,reflection:.9,shine:.6,follow:.12})})}function Ge(){Xe(),document.addEventListener("crawler:palette-open",()=>{fe&&fe.resize()})}function Be(){if(!document.body.classList.contains("api-page"))return;let d=document.createElement("canvas");d.setAttribute("layoutsubtree",""),d.className="grid-source";let f=document.createElement("canvas");f.className="grid-bg",document.body.appendChild(d),document.body.appendChild(f),me("Grid",()=>{xe({source:d,content:document.body,output:f},{tileSize:56,gap:1,cornerRadius:4,amplitude:1.1,waveSpeed:.55,frequency:4,waveWidth:.18,fadeTime:2.2,maxLift:.28,jitter:.3,liftHeight:6,perspective:1e3,tilt:.15,shading:.4,tint:[.13,.55,.38],tintStrength:.22,idleRipples:0})})}function Te(){Fe(),Ge(),Be()}document.readyState==="loading"?document.addEventListener("DOMContentLoaded",Te):Te();export{Te as init};
