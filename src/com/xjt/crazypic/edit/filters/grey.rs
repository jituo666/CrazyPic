#pragma version(1)
#pragma rs java_package_name(com.xjt.crazypic.edit.filters)

uchar __attribute__((kernel)) RGBAtoA(uchar4 in) {
    return in.r;
}
