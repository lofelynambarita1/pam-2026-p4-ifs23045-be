package org.delcom.module

import org.delcom.repositories.DrakorRepository
import org.delcom.repositories.IDrakorRepository
import org.delcom.services.DrakorService
import org.delcom.services.ProfileService
import org.koin.dsl.module

val appModule = module {
    // Drakor Repository
    single<IDrakorRepository> {
        DrakorRepository()
    }

    // Drakor Service
    single {
        DrakorService(get())
    }

    // Profile Service
    single {
        ProfileService()
    }
}