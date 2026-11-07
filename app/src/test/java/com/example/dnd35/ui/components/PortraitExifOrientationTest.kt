package com.example.dnd35.ui.components

import android.graphics.Bitmap
import androidx.exifinterface.media.ExifInterface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PortraitExifOrientationTest {
    @Test
    fun normalOrientationKeepsOriginalBitmap() {
        val source = Bitmap.createBitmap(20, 10, Bitmap.Config.ARGB_8888)

        assertSame(source, applyExifOrientation(source, ExifInterface.ORIENTATION_NORMAL))
    }

    @Test
    fun quarterTurnsAndTransposedOrientationsSwapDimensions() {
        val orientations = listOf(
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_TRANSVERSE,
            ExifInterface.ORIENTATION_ROTATE_270
        )

        orientations.forEach { orientation ->
            val source = Bitmap.createBitmap(20, 10, Bitmap.Config.ARGB_8888)
            val result = applyExifOrientation(source, orientation)

            assertEquals("orientation=$orientation", 10, result.width)
            assertEquals("orientation=$orientation", 20, result.height)
            if (result !== source) result.recycle()
            source.recycle()
        }
    }

    @Test
    fun horizontalAndVerticalFlipsKeepDimensions() {
        val orientations = listOf(
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            ExifInterface.ORIENTATION_FLIP_VERTICAL
        )

        orientations.forEach { orientation ->
            val source = Bitmap.createBitmap(20, 10, Bitmap.Config.ARGB_8888)
            val result = applyExifOrientation(source, orientation)

            assertEquals(20, result.width)
            assertEquals(10, result.height)
            if (result !== source) result.recycle()
            source.recycle()
        }
    }
}
