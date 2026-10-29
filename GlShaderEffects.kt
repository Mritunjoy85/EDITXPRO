package com.editx.pro.engine

object GlShaderEffects {
    const val VERTEX_SHADER = """
        attribute vec4 aFramePosition;
        attribute vec4 aTexCoords;
        varying vec2 vTexCoords;
        uniform mat4 uTransformationMatrix;
        void main() {
            gl_Position = uTransformationMatrix * aFramePosition;
            vTexCoords = aTexCoords.xy;
        }
    """

    const val COLOR_ADJUST_FRAGMENT_SHADER = """
        precision mediump float;
        varying vec2 vTexCoords;
        uniform sampler2D uTexSampler;
        uniform float uBrightness;
        uniform float uContrast;
        uniform float uSaturation;
        uniform float uTemperature;
        uniform float uVignette;

        void main() {
            vec4 color = texture2D(uTexSampler, vTexCoords);
            vec3 rgb = color.rgb;
            rgb += vec3(uBrightness);
            rgb = (rgb - 0.5) * (1.0 + uContrast) + 0.5;
            float gray = dot(rgb, vec3(0.299, 0.587, 0.114));
            rgb = mix(vec3(gray), rgb, 1.0 + uSaturation);
            rgb.r += uTemperature * 0.15;
            rgb.b -= uTemperature * 0.15;
            if (uVignette > 0.0) {
                float dist = distance(vTexCoords, vec2(0.5, 0.5));
                float vig = smoothstep(0.8, 0.5 - (uVignette * 0.35), dist);
                rgb *= vig;
            }
            gl_FragColor = vec4(clamp(rgb, 0.0, 1.0), color.a);
        }
    """

    const val GLITCH_FRAGMENT_SHADER = """
        precision mediump float;
        varying vec2 vTexCoords;
        uniform sampler2D uTexSampler;
        uniform float uTime;
        uniform float uIntensity;

        float random(vec2 st) {
            return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453);
        }
        void main() {
            vec2 uv = vTexCoords;
            float glitchAmount = uIntensity * 0.04;
            float sliceY = floor(uv.y * 30.0);
            if (random(vec2(sliceY, floor(uTime * 15.0))) > 0.8) {
                uv.x += (random(vec2(sliceY, uTime)) - 0.5) * glitchAmount * 2.0;
            }
            float r = texture2D(uTexSampler, uv + vec2(glitchAmount, 0.0)).r;
            float g = texture2D(uTexSampler, uv).g;
            float b = texture2D(uTexSampler, uv - vec2(glitchAmount, 0.0)).b;
            gl_FragColor = vec4(r, g, b, 1.0);
        }
    """
}