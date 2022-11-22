precision mediump float;

varying vec2 transferValue; //与顶点着色器中传值
uniform sampler2D inputImageTexture; //图片

// 模糊半径
// uniform int uBlurRadius = 25;
const int uBlurRadius = 25;
// 模糊步长
uniform vec2 uXBlurOffset;
uniform vec2 uYBlurOffset;
// const float vec2 uBlurOffset = 1.0f / 500.0f;
// const float vec2 uBlurOffset = vec2(1.0f / 500.0f,1.0f / 500.0f);
// const float vec2 uBlurOffset = vec2(1.0f / 500.0f,1.0f / 500.0f);
// const float uBlurOffset[2] = float[2](1.0f / 500.0f,1.0f / 500.0f);

// 总权重
const float uSumWeight = 0.9690602;
// PI
const float PI = 3.1415926;

// 边界值处理
vec2 clampCoordinate(vec2 coordinate) {
    return vec2(clamp(coordinate.x, 0.0, 1.0), clamp(coordinate.y, 0.0, 1.0));
}

// 计算权重
float getWeight(int i) {
    float sigma = float(25) *0.4f + 0.6f;
    return (1.0 / sqrt(2.0 * PI * sigma * sigma)) * exp(-float(i * i) / (2.0 * sigma * sigma)) / uSumWeight;
}



void main(){
    vec4 sourceColor = texture2D(inputImageTexture, transferValue);

    if (25 <= 1) {
        gl_FragColor = sourceColor;
        return;
    }

    float weight = getWeight(0);

    vec3 firstColor = sourceColor.rgb * weight;

    for (int i = 1; i < uBlurRadius; i++) {
        weight = getWeight(i);
        firstColor += texture2D(inputImageTexture, clampCoordinate(transferValue - uXBlurOffset * float(i))).rgb * weight;
        firstColor += texture2D(inputImageTexture, clampCoordinate(transferValue + uXBlurOffset * float(i))).rgb * weight;
    }

    vec3 finalColor = firstColor.rgb * weight;
    for (int i = 1; i < uBlurRadius; i++) {
        weight = getWeight(i);
        finalColor += texture2D(inputImageTexture, clampCoordinate(transferValue - uYBlurOffset * float(i))).rgb * weight;
        finalColor += texture2D(inputImageTexture, clampCoordinate(transferValue + uYBlurOffset * float(i))).rgb * weight;
    }

    gl_FragColor = vec4(finalColor, sourceColor.a);
}