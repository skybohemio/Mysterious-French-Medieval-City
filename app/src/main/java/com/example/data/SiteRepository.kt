package com.example.data

import kotlinx.coroutines.flow.Flow

class SiteRepository(
    private val siteDao: SiteDao,
    private val routeDao: RouteDao,
    private val offlineMapDao: OfflineMapDao,
    private val userDao: UserDao,
    private val purchaseDao: PurchaseDao
) {
    val allSites: Flow<List<Site>> = siteDao.getAllSitesFlow()
    val allRoutes: Flow<List<TourRoute>> = routeDao.getAllRoutesFlow()
    val cachedMapTiles: Flow<List<OfflineMapData>> = offlineMapDao.getAllCachedMapTilesFlow()
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    val allPurchases: Flow<List<PurchaseOrder>> = purchaseDao.getAllPurchases()

    suspend fun insert(site: Site): Long {
        return siteDao.insertSite(site)
    }

    suspend fun insertSites(sites: List<Site>): List<Long> {
        return siteDao.insertSites(sites)
    }

    suspend fun getSiteCount(): Int {
        return siteDao.getSiteCount()
    }

    suspend fun delete(site: Site) {
        siteDao.deleteSite(site)
    }

    suspend fun deleteById(id: Int) {
        siteDao.deleteSiteById(id)
    }

    suspend fun getSiteById(id: Int): Site? {
        return siteDao.getSiteById(id)
    }

    // Route functions
    suspend fun insertRoute(route: TourRoute): Long {
        return routeDao.insertRoute(route)
    }

    suspend fun deleteRoute(route: TourRoute) {
        routeDao.deleteRoute(route)
    }

    suspend fun updateRoute(route: TourRoute) {
        routeDao.updateRoute(route)
    }

    suspend fun deleteRouteById(id: Int) {
        routeDao.deleteRouteById(id)
    }

    suspend fun getRouteById(id: Int): TourRoute? {
        return routeDao.getRouteById(id)
    }

    // Map Cache functions
    suspend fun saveMapTile(tile: OfflineMapData) {
        offlineMapDao.insertTile(tile)
    }

    suspend fun saveMapTiles(tiles: List<OfflineMapData>) {
        offlineMapDao.insertTiles(tiles)
    }

    suspend fun clearMapTiles() {
        offlineMapDao.clearAllMapCache()
    }

    // User Authentication functions
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    suspend fun getUserCount(): Int {
        return userDao.getUserCount()
    }

    // Purchase & Stripe functions
    suspend fun insertPurchase(purchase: PurchaseOrder): Long {
        return purchaseDao.insertPurchase(purchase)
    }

    fun getPurchasesForUser(email: String): Flow<List<PurchaseOrder>> {
        return purchaseDao.getPurchasesForUser(email)
    }

    suspend fun hasUserPurchasedRoute(email: String, routeId: Int): Boolean {
        return purchaseDao.hasUserPurchasedRoute(email, routeId)
    }

    suspend fun getTotalRevenue(): Double {
        return purchaseDao.getTotalRevenue() ?: 0.0
    }

    suspend fun getPurchaseCount(): Int {
        return purchaseDao.getPurchaseCount()
    }
}
