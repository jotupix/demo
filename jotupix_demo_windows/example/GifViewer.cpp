#include "pch.h"
#include "GifViewer.h"

CGifViewer::CGifViewer()
{
    m_pImage = nullptr;
    m_currentFrame = 0;
    m_frameDelay = 100;
    m_bPlaying = FALSE;

    GdiplusStartupInput gdiplusStartupInput;
    GdiplusStartup(&m_gdiplusToken, &gdiplusStartupInput, NULL);
}

CGifViewer::~CGifViewer()
{
    Stop();
    if (m_pImage)
        delete m_pImage;
    GdiplusShutdown(m_gdiplusToken);
}

BEGIN_MESSAGE_MAP(CGifViewer, CStatic)
    ON_WM_PAINT()
    ON_WM_TIMER()
END_MESSAGE_MAP()

BOOL CGifViewer::LoadGif(CString strPath)
{
    Stop();

    if (m_pImage)
    {
        delete m_pImage;
        m_pImage = nullptr;
    }

    // Clear display area
    CClientDC dc(this);
    CRect rect;
    GetClientRect(&rect);
    dc.FillSolidRect(&rect, RGB(0xF0, 0xF0, 0xF0)); 

    // The file is read into memory, and an image is created from the memory data to prevent the file from being occupied.
    CFile file;
    if (!file.Open(strPath, CFile::modeRead | CFile::typeBinary))
    {
        return FALSE;
    }

    ULONGLONG fileSize = file.GetLength();
    BYTE* pBuffer = new BYTE[(UINT)fileSize];
    file.Read(pBuffer, (UINT)fileSize);
    file.Close();

    IStream* pStream = SHCreateMemStream(pBuffer, (UINT)fileSize);
    delete[] pBuffer;

    if (!pStream)
    {
        return FALSE;
    }

    m_pImage = Image::FromStream(pStream);
    pStream->Release();

    // Create via file
    //m_pImage = Image::FromFile(strPath);
    //if (!m_pImage || m_pImage->GetLastStatus() != Ok)
    //    return FALSE;

    // Get the number of frames
    UINT count = 0;
    count = m_pImage->GetFrameDimensionsCount();
    GUID* pDimensionIDs = new GUID[count];
    m_pImage->GetFrameDimensionsList(pDimensionIDs, count);

    m_guidFrameDimension = pDimensionIDs[0];
    delete[] pDimensionIDs;

    m_frameCount = m_pImage->GetFrameCount(&m_guidFrameDimension);

    // Get the delay time per frame
    UINT size = m_pImage->GetPropertyItemSize(PropertyTagFrameDelay);
    if (size)
    {
        PropertyItem* pItem = (PropertyItem*)malloc(size);
        if (pItem)
        {
            m_pImage->GetPropertyItem(PropertyTagFrameDelay, size, pItem);
            // Get the first frame delay
            m_frameDelay = ((UINT*)pItem->value)[0] * 10; // Unit: 1/100 second
            free(pItem);
        }
    }

    Invalidate();
    return TRUE;
}

void CGifViewer::Start()
{
    if (m_pImage && !m_bPlaying)
    {
        SetTimer(1, m_frameDelay, NULL);
        m_bPlaying = TRUE;
    }
}

void CGifViewer::Stop()
{
    if (m_bPlaying)
    {
        KillTimer(1);
        m_bPlaying = FALSE;
    }
}

BOOL CGifViewer::GetGifInfo(GIF_INFO& info)
{
    if (!m_pImage)
        return FALSE;

    ZeroMemory(&info, sizeof(info));

    info.width = m_pImage->GetWidth();
    info.height = m_pImage->GetHeight();
    info.frameCount = m_frameCount;

    return TRUE;
}

void CGifViewer::OnPaint()
{
    CPaintDC dc(this);
    Graphics g(dc);
    GetClientRect(&m_clientRect);
    DrawFrame(g);
}

#if 0
void CGifViewer::DrawFrame(Graphics& g)
{
    if (!m_pImage)
        return;
    g.DrawImage(m_pImage, m_clientRect.left, m_clientRect.top,
        m_clientRect.Width(), m_clientRect.Height());
}
#else
void CGifViewer::DrawFrame(Graphics& g)
{
    if (!m_pImage)
        return;

    int imgW = m_pImage->GetWidth();
    int imgH = m_pImage->GetHeight();

    int wndW = m_clientRect.Width();
    int wndH = m_clientRect.Height();

    if (imgW <= 0 || imgH <= 0 || wndW <= 0 || wndH <= 0)
        return;

    // Calculate scaling ratio (preserving aspect ratio)
    double scaleX = static_cast<double>(wndW) / imgW;
    double scaleY = static_cast<double>(wndH) / imgH;
    double scale = min(scaleX, scaleY);

    // Calculate the size of the drawing area
    int drawW = static_cast<int>(imgW * scale);
    int drawH = static_cast<int>(imgH * scale);

    // Calculate center offset
    int offsetX = m_clientRect.left + (wndW - drawW) / 2;
    int offsetY = m_clientRect.top + (wndH - drawH) / 2;

    // Draw an image (maintain proportions and center it).
    g.DrawImage(m_pImage, offsetX, offsetY, drawW, drawH);
}

#endif

void CGifViewer::OnTimer(UINT_PTR nIDEvent)
{
    NextFrame();
    Invalidate(FALSE);
}

void CGifViewer::NextFrame()
{
    if (!m_pImage || m_frameCount <= 1)
        return;

    m_currentFrame = (m_currentFrame + 1) % m_frameCount;
    m_pImage->SelectActiveFrame(&m_guidFrameDimension, m_currentFrame);
}
