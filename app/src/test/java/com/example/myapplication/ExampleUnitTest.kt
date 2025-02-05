package com.example.myapplication

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class ExampleUnitTest {

    @Test
    fun testOptimizedProductCouponMatching() {
        // 定義產品列表
        val products = listOf(
            Product("Product1", 100, 1),
            Product("Product2", 300, 2), 
            Product("Product3", 150, 1)
        )
        // 定義優惠券列表
        val coupons = listOf(
            Coupon("Coupon1", 150, 1, listOf("Product1", "Product3"), LocalDate.of(2023, 12, 31)),
            Coupon("Coupon2", 100, 5, listOf("Product1", "Product2"), LocalDate.of(2023, 11, 30)),
            Coupon("Coupon3", 200, 2, listOf("Product2"), LocalDate.of(2023, 10, 31)),
            Coupon("Coupon4", 450, 1, listOf("Product1", "Product2", "Product3"), LocalDate.of(2023, 9, 30))
        )

        // 執行產品和優惠券的最佳匹配
        val result = optimizedMatchProductsAndCoupons(products, coupons)

        // 驗證結果
        assertEquals(4, result.size)
        // 驗證一張優惠券換一個產品
        assertEquals(1, result[0].coupons.size)
        // 驗證產品2 有使用一張Coupon3
        assertTrue(result.filter { it.product.name == "Product2" }?.any { it.coupons.any { it.name == "Coupon3" } } ?: false)

        assertTrue(result.filter { it.product.name == "Product1" }?.any { it.coupons.any { it.name == "Coupon2" } } ?: false)
    }
}

// 定義產品數據類
data class Product(val name: String, val price: Int, val quantity: Int)
// 定義優惠券數據類
data class Coupon(val name: String, var value: Int, var quantity: Int, val applicableProducts: List<String>, val expiryDate: LocalDate)
// 定義匹配的優惠券數據類
data class MatchedCoupon(val name: String, var value: Int, var quantity: Int, val applicableProducts: List<String>, val expiryDate: LocalDate, val usedQuantity: Int)
// 定義匹配結果數據類
data class MatchResult(val product: Product, var coupons: List<MatchedCoupon>)

// 最佳匹配產品和優惠券的函數
fun optimizedMatchProductsAndCoupons(products: List<Product>, coupons: List<Coupon>): List<MatchResult> {
    // 按價格降序排列產品
    val sortedProducts = products.sortedByDescending { it.price }
    // 按價值降序排列優惠券，若價值相同則按到期日升序排列
    val sortedCoupons = coupons.sortedWith(compareByDescending<Coupon> { it.value }.thenBy { it.expiryDate }).toMutableList()
    val result = mutableListOf<MatchResult>()

    for (product in sortedProducts) {
        var remainingQuantity = product.quantity
        while (remainingQuantity > 0) {
            // 篩選適用於當前產品且數量大於0的優惠券
            val applicableCoupons = sortedCoupons.filter { it.applicableProducts.contains(product.name) && it.quantity > 0 }
            if (applicableCoupons.isNotEmpty()) {
                // 找到價值最高的優惠券
                val bestCoupon = applicableCoupons.maxByOrNull { it.value } ?: continue
                // 計算實際使用的數量
                val usedQuantity = minOf(remainingQuantity, bestCoupon.quantity)
                // 創建匹配的優惠券
                val matchedCoupon = MatchedCoupon(bestCoupon.name, bestCoupon.value, bestCoupon.quantity, bestCoupon.applicableProducts, bestCoupon.expiryDate, usedQuantity)
                // 將匹配結果添加到結果列表中
                result.add(MatchResult(product.copy(quantity = usedQuantity), listOf(matchedCoupon)))
                // 減少優惠券的數量
                bestCoupon.quantity -= usedQuantity
                // 減少產品的剩餘數量
                remainingQuantity -= usedQuantity
            } else {
                break
            }
        }
    }

    return result
}
