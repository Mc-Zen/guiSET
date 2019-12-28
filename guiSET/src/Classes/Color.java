package pGUI.classes;



public class Color {

  public static final int create(int gray) {
    if (gray > 255) gray = 255; 
    else if (gray < 0) gray = 0;
    return 0xff000000 | (gray << 16) | (gray << 8) | gray;
  }

  public static final int create(float fgray) {
    int gray = (int) fgray;
    if (gray > 255) gray = 255; 
    else if (gray < 0) gray = 0;
    return 0xff000000 | (gray << 16) | (gray << 8) | gray;
  }



  public static final int create(int gray, int alpha) {
    if (alpha > 255) alpha = 255; 
    else if (alpha < 0) alpha = 0;
    if (gray > 255) {
      // then assume this is actually a #FF8800
      return (alpha << 24) | (gray & 0xFFFFFF);
    } else {
      //if (gray > 255) gray = 255; else if (gray < 0) gray = 0;
      return (alpha << 24) | (gray << 16) | (gray << 8) | gray;
    }
  }


  public static final int create(float fgray, float falpha) {
    int gray = (int) fgray;
    int alpha = (int) falpha;
    if (gray > 255) gray = 255; 
    else if (gray < 0) gray = 0;
    if (alpha > 255) alpha = 255; 
    else if (alpha < 0) alpha = 0;
    return (alpha << 24) | (gray << 16) | (gray << 8) | gray;
  }



  public static final int create(int v1, int v2, int v3) {
    if (v1 > 255) v1 = 255; 
    else if (v1 < 0) v1 = 0;
    if (v2 > 255) v2 = 255; 
    else if (v2 < 0) v2 = 0;
    if (v3 > 255) v3 = 255; 
    else if (v3 < 0) v3 = 0;

    return 0xff000000 | (v1 << 16) | (v2 << 8) | v3;
  }


  public static final int create(int v1, int v2, int v3, int alpha) {
    if (alpha > 255) alpha = 255; 
    else if (alpha < 0) alpha = 0;
    if (v1 > 255) v1 = 255; 
    else if (v1 < 0) v1 = 0;
    if (v2 > 255) v2 = 255; 
    else if (v2 < 0) v2 = 0;
    if (v3 > 255) v3 = 255; 
    else if (v3 < 0) v3 = 0;

    return (alpha << 24) | (v1 << 16) | (v2 << 8) | v3;
  }


  public static final int create(float v1, float v2, float v3) {
    if (v1 > 255) v1 = 255; 
    else if (v1 < 0) v1 = 0;
    if (v2 > 255) v2 = 255; 
    else if (v2 < 0) v2 = 0;
    if (v3 > 255) v3 = 255; 
    else if (v3 < 0) v3 = 0;

    return 0xff000000 | ((int)v1 << 16) | ((int)v2 << 8) | (int)v3;
  }


  public static final int create(float v1, float v2, float v3, float alpha) {
    if (alpha > 255) alpha = 255; 
    else if (alpha < 0) alpha = 0;
    if (v1 > 255) v1 = 255; 
    else if (v1 < 0) v1 = 0;
    if (v2 > 255) v2 = 255; 
    else if (v2 < 0) v2 = 0;
    if (v3 > 255) v3 = 255; 
    else if (v3 < 0) v3 = 0;

    return ((int)alpha << 24) | ((int)v1 << 16) | ((int)v2 << 8) | (int)v3;
  }
}

