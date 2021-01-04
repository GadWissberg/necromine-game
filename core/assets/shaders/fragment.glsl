#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#ifdef normalFlag
varying vec3 v_normal;
#endif//normalFlag

#if defined(colorFlag)
varying vec4 v_color;
#endif

#ifdef blendedFlag
varying float v_opacity;
#ifdef alphaTestFlag
varying float v_alphaTest;
#endif//alphaTestFlag
#endif//blendedFlag

#if defined(diffuseTextureFlag) || defined(specularTextureFlag) || defined(emissiveTextureFlag)
#define textureFlag
#endif

#ifdef diffuseTextureFlag
varying MED vec2 v_diffuseUV;
#endif

#ifdef specularTextureFlag
varying MED vec2 v_specularUV;
#endif

#ifdef emissiveTextureFlag
varying MED vec2 v_emissiveUV;
#endif

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

#ifdef specularColorFlag
uniform vec4 u_specularColor;
#endif

#ifdef specularTextureFlag
uniform sampler2D u_specularTexture;
#endif

#ifdef normalTextureFlag
uniform sampler2D u_normalTexture;
#endif

#ifdef emissiveColorFlag
uniform vec4 u_emissiveColor;
#endif

#ifdef emissiveTextureFlag
uniform sampler2D u_emissiveTexture;
#endif

#ifdef lightingFlag
varying vec3 v_lightDiffuse;

#if    defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif//ambientFlag

#ifdef specularFlag
varying vec3 v_lightSpecular;
#endif//specularFlag

#ifdef shadowMapFlag
uniform sampler2D u_shadowTexture;
uniform float u_shadowPCFOffset;
varying vec3 v_shadowMapUv;
#define separateAmbientFlag


float getShadowness(vec2 offset)
{
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0);
    return step(v_shadowMapUv.z, dot(texture2D(u_shadowTexture, v_shadowMapUv.xy + offset), bitShifts));//+(1.0/255.0));
}

float getShadow()
{
    return (
    getShadowness(vec2(u_shadowPCFOffset, u_shadowPCFOffset)) +
    getShadowness(vec2(-u_shadowPCFOffset, u_shadowPCFOffset)) +
    getShadowness(vec2(u_shadowPCFOffset, -u_shadowPCFOffset)) +
    getShadowness(vec2(-u_shadowPCFOffset, -u_shadowPCFOffset))) * 0.25;
}
    #endif//shadowMapFlag

    #if defined(ambientFlag) && defined(separateAmbientFlag)
varying vec3 v_ambientLight;
#endif//separateAmbientFlag

#endif//lightingFlag

#ifdef fogFlag
uniform vec4 u_fogColor;
varying float v_fog;
#endif// fogFlag

varying vec3 v_frag_pos;
uniform vec3 u_lights_positions[8];
uniform vec2 u_lights_extra_data[8];
uniform int u_number_of_lights;
uniform int u_model_width;
uniform int u_model_height;
uniform float u_fow_map[16];
uniform int u_model_x;
uniform int u_model_y;

float ramp(float value, float max, float high, float low){
    float ramped = value;
    if (value >= high && value < max){
        ramped = high;
    } else if (value >= low){
        ramped = low;
    } else if (value >= 0.0){
        ramped = 0.0;
    }
    return ramped;
}

void main() {
    #if defined(normalFlag)
    vec3 normal = v_normal;
    #endif// normalFlag

    #if defined(diffuseTextureFlag) && defined(diffuseColorFlag) && defined(colorFlag)
    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor * v_color;
    #elif defined(diffuseTextureFlag) && defined(diffuseColorFlag)
    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor;
    #elif defined(diffuseTextureFlag) && defined(colorFlag)
    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * v_color;
    #elif defined(diffuseTextureFlag)
    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV);
    #elif defined(diffuseColorFlag) && defined(colorFlag)
    vec4 diffuse = u_diffuseColor * v_color;
    #elif defined(diffuseColorFlag)
    vec4 diffuse = u_diffuseColor;
    #elif defined(colorFlag)
    vec4 diffuse = v_color;
    #else
    vec4 diffuse = vec4(1.0);
    #endif

    #if defined(emissiveTextureFlag) && defined(emissiveColorFlag)
    vec4 emissive = texture2D(u_emissiveTexture, v_emissiveUV) * u_emissiveColor;
    #elif defined(emissiveTextureFlag)
    vec4 emissive = texture2D(u_emissiveTexture, v_emissiveUV);
    #elif defined(emissiveColorFlag)
    vec4 emissive = u_emissiveColor;
    #else
    vec4 emissive = vec4(0.0);
    #endif

    #if (!defined(lightingFlag))
    gl_FragColor.rgb = diffuse.rgb + emissive.rgb;
    #elif (!defined(specularFlag))
    #if defined(ambientFlag) && defined(separateAmbientFlag)
    #ifdef shadowMapFlag

    int numberOfRows = int(max(v_frag_pos.z, 0.0) - float(max(u_model_y, 0.0)))*u_model_width;
    int horizontalOffset = int(max(v_frag_pos.x, 0.0)-float(max(u_model_x, 0.0)));
    int fragPosInModel = numberOfRows + horizontalOffset;
    if (horizontalOffset != u_model_width && u_fow_map[fragPosInModel] == 1.0){
        if (u_number_of_lights > -1){
            gl_FragColor.rgb = diffuse.rgb * v_lightDiffuse;
            for (int i = 0; i< u_number_of_lights; i++){
                vec3 light =u_lights_positions[i];
                vec3 sub = light.xyz - v_frag_pos.xyz;
                vec3 lightDir = normalize(sub);
                float distance = length(sub);
                vec2 extra = u_lights_extra_data[i];
                float attenuation = 6.0 * extra.x / (1.0 + (0.1*distance) + (0.44*distance*distance));
                float dot_value = dot(v_normal, lightDir);
                float intensity = max(ramp(dot_value, 1.0, 1.0 - extra.y/10.0, 1.0 - extra.y/10.0-0.2), 0.0);
                gl_FragColor.rgb += (diffuse.rgb * (attenuation * intensity));
            }
            gl_FragColor.rgb = (getShadow() == 0.0 ? gl_FragColor.rgb * 0.5 : gl_FragColor.rgb) + emissive.rgb;
        }
    } else {
        gl_FragColor.rgb = vec3(0.0);
    }
    if (u_number_of_lights == -1) {
        gl_FragColor.rgb = diffuse.rgb + emissive.rgb;
    }
        #else
    gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + v_lightDiffuse)) + emissive.rgb;
    #endif//shadowMapFlag
    #else
    #ifdef shadowMapFlag
    //    gl_FragColor.rgb = getShadow() * (diffuse.rgb * v_lightDiffuse) + emissive.rgb;
    #else


    #endif//shadowMapFlag
    #endif
    #else
    #if defined(specularTextureFlag) && defined(specularColorFlag)
    vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * u_specularColor.rgb * v_lightSpecular;
    #elif defined(specularTextureFlag)
    vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * v_lightSpecular;
    #elif defined(specularColorFlag)
    vec3 specular = u_specularColor.rgb * v_lightSpecular;
    #else
    vec3 specular = v_lightSpecular;
    #endif

    #if defined(ambientFlag) && defined(separateAmbientFlag)
    #ifdef shadowMapFlag
    gl_FragColor.rgb = (diffuse.rgb * (getShadow() * v_lightDiffuse + v_ambientLight)) + specular + emissive.rgb;
    //gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
    #else
    gl_FragColor.rgb = (diffuse.rgb * (v_lightDiffuse + v_ambientLight)) + specular + emissive.rgb;
    #endif//shadowMapFlag
    #else
    #ifdef shadowMapFlag
    gl_FragColor.rgb = getShadow() * ((diffuse.rgb * v_lightDiffuse) + specular) + emissive.rgb;
    #else
    gl_FragColor.rgb = (diffuse.rgb * v_lightDiffuse) + specular + emissive.rgb;
    #endif//shadowMapFlag
    #endif
    #endif//lightingFlag

    #ifdef fogFlag
    gl_FragColor.rgb = mix(gl_FragColor.rgb, u_fogColor.rgb, v_fog);
    #endif// end fogFlag

    #ifdef blendedFlag
    gl_FragColor.a = diffuse.a * v_opacity;
    #ifdef alphaTestFlag
    if (gl_FragColor.a <= v_alphaTest)
    discard;
    #endif
    #else
    gl_FragColor.a = 1.0;
    #endif

}