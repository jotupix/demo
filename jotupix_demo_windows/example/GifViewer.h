#pragma once
#include <gdiplus.h>
using namespace Gdiplus;

typedef struct _GIF_INFO
{
    UINT width;   
    UINT height;  
    UINT frameCount; 
} GIF_INFO;

class CGifViewer : public CStatic
{
public:
    CGifViewer();
    virtual ~CGifViewer();

    BOOL LoadGif(CString strPath);
    void Start();
    void Stop();
    BOOL GetGifInfo(GIF_INFO& info);

protected:
    ULONG_PTR m_gdiplusToken;
    Image* m_pImage;
    UINT m_frameCount;
    UINT m_currentFrame;
    UINT m_frameDelay;  // milliseconds
    GUID m_guidFrameDimension;
    CRect m_clientRect;
    BOOL m_bPlaying;

    void DrawFrame(Graphics& g);
    void NextFrame();

protected:
    afx_msg void OnPaint();
    afx_msg void OnTimer(UINT_PTR nIDEvent);
    DECLARE_MESSAGE_MAP()

private:
    GIF_INFO m_gifInfo;
};
