package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BackupManager
import com.example.data.CncDesignEntity
import com.example.security.SecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CNC Craft", appName)
    }

    @Test
    fun `verify profit and margin calculation`() {
        val design = CncDesignEntity(
            title = "Carved Teak Door",
            category = "Door",
            sellingPrice = 500.0,
            productionCost = 150.0,
            dateString = "2026-10-21"
        )

        assertEquals(350.0, design.profit, 0.001)
        assertEquals(70.0, design.profitMarginPercent, 0.001)
    }

    @Test
    fun `verify backup json serialization and deserialization`() {
        val originalList = listOf(
            CncDesignEntity(
                title = "Geometric Jali Screen",
                category = "Jali / Grill",
                sellingPrice = 280.0,
                productionCost = 80.0,
                material = "HPL",
                dateString = "2026-10-21"
            )
        )

        val jsonString = BackupManager.exportToJsonString(originalList)
        assertTrue(jsonString.contains("Geometric Jali Screen"))

        val restored = BackupManager.parseJsonBackup(jsonString)
        assertEquals(1, restored.size)
        assertEquals("Geometric Jali Screen", restored[0].title)
        assertEquals(280.0, restored[0].sellingPrice, 0.001)
        assertEquals(80.0, restored[0].productionCost, 0.001)
        assertEquals(200.0, restored[0].profit, 0.001)
    }

    @Test
    fun `verify security manager pin verification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val securityManager = SecurityManager(context)

        securityManager.pinCode = "5829"
        assertTrue(securityManager.verifyPin("5829"))
        assertFalse(securityManager.verifyPin("0000"))
    }
}
