package com.kejing.spatialarcade.ui.feature.arcade
import com.kejing.spatialarcade.domain.model.GameSnapshot
data class ArcadeUiState(val game:GameSnapshot=GameSnapshot(),val selector:Boolean=false,val pending:Int=2,val muted:Boolean=false,val ready:Boolean=false,val error:String?=null,val library:Boolean=false,val english:Boolean=false)
sealed interface ArcadeEvent {
    data object OpenSelector:ArcadeEvent;data object Confirm:ArcadeEvent;data object Cancel:ArcadeEvent
    data class Select(val id:Int):ArcadeEvent;data object Pause:ArcadeEvent;data object Resume:ArcadeEvent
    data object ToggleLanguage:ArcadeEvent;data object MusicPlayback:ArcadeEvent;data object Reset:ArcadeEvent;data object ToggleSound:ArcadeEvent;data object Demo:ArcadeEvent
    data class Layout(val index:Int):ArcadeEvent
    data object OpenLibrary:ArcadeEvent;data object CloseLibrary:ArcadeEvent
    data class Track(val index:Int):ArcadeEvent
}
