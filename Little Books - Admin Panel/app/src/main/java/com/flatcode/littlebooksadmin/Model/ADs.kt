package com.flatcode.littlebooksadmin.Modelimport

class ADs {
    var name: String? = null
    var adsLoadedCount = 0
    var adsClickedCount = 0

    constructor()

    constructor(name: String?, adsLoadedCount: Int, adsClickedCount: Int) {
        this.name = name
        this.adsLoadedCount = adsLoadedCount
        this.adsClickedCount = adsClickedCount
    }
}