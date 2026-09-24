package com.kejing.spatialarcade.ui.feature.arcade
import androidx.lifecycle.ViewModel
import com.kejing.spatialarcade.domain.model.*
import com.kejing.spatialarcade.domain.usecase.GameRulesUseCase
import com.kejing.spatialarcade.data.repository.GameRepository
import com.kejing.spatialarcade.data.repository.LocalGameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ArcadeViewModel(val kind:GameKind,repository:GameRepository=LocalGameRepository()):ViewModel(){
    val contacts=repository.contacts()
    val engine=GameRulesUseCase(kind,contacts,repository.score())
    private val _state=MutableStateFlow(ArcadeUiState(game=engine.snapshot,selector=kind==GameKind.A,library=kind==GameKind.C))
    val state=_state.asStateFlow()
    fun sync(){_state.value=_state.value.copy(game=engine.snapshot)}
    fun loading(){_state.value=_state.value.copy(ready=false,error=null)}
    fun ready(){_state.value=_state.value.copy(ready=true)}
    fun error(message:String){_state.value=_state.value.copy(error=message)}
    fun onEvent(event:ArcadeEvent){when(event){
        ArcadeEvent.OpenSelector->_state.value=_state.value.copy(selector=kind==GameKind.A)
        is ArcadeEvent.Select->{if(event.id in contacts.indices){engine.setContact(event.id);_state.value=_state.value.copy(pending=event.id)}}
        ArcadeEvent.Confirm->Unit // Older debug clients remain harmless; selection is immediate.
        ArcadeEvent.Cancel->Unit
        ArcadeEvent.Pause->{if(kind!=GameKind.A)engine.pause()};ArcadeEvent.Resume->{if(kind!=GameKind.A)engine.resume()};ArcadeEvent.Reset->{if(kind==GameKind.D||kind==GameKind.A)engine.setLayout(0)else engine.reset()}
        ArcadeEvent.ToggleLanguage->_state.value=_state.value.copy(english=!_state.value.english)
        ArcadeEvent.MusicPlayback->{if(kind==GameKind.C&&!_state.value.library&&_state.value.ready)engine.toggleMusicPlayback()}
        ArcadeEvent.ToggleSound->_state.value=_state.value.copy(muted=!_state.value.muted)
        ArcadeEvent.Demo->{_state.value=_state.value.copy(library=false);engine.demo()}
        is ArcadeEvent.Layout->{if(kind!=GameKind.A)engine.setLayout(event.index);_state.value=_state.value.copy(library=false)}
        is ArcadeEvent.Track->{engine.selectTrack(event.index);_state.value=_state.value.copy(library=false)}
        ArcadeEvent.OpenLibrary->{if(kind==GameKind.C){engine.pause();_state.value=_state.value.copy(library=true)}}
        ArcadeEvent.CloseLibrary->_state.value=_state.value.copy(library=false)
    };sync()}
}
