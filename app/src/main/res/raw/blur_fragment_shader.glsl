precision mediump float;

varying vec2 transferValue; //与顶点着色器中传值
uniform sampler2D inputImageTexture; //图片

// 模糊半径
// uniform int uBlurRadius = 25;
const int uBlurRadius = 25;
// 模糊步长
uniform vec2 uBlurOffset;
// 总权重
const float uSumWeight = 0.9792321f;
// PI
const float PI = 3.1415926535897932f;

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

    vec3 finalColor = sourceColor.rgb * weight;

    for (int i = 0; i <= uBlurRadius; i++) {
        weight = getWeight(i);
        finalColor += texture2D(inputImageTexture, clampCoordinate(transferValue - uBlurOffset * float(i))).rgb * weight;
        finalColor += texture2D(inputImageTexture, clampCoordinate(transferValue + uBlurOffset * float(i))).rgb * weight;
    }

    gl_FragColor = vec4(finalColor, sourceColor.a);
}