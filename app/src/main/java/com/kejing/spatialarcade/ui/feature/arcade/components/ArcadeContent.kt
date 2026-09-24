package com.kejing.spatialarcade.ui.feature.arcade.components
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kejing.spatialarcade.domain.model.*
import com.kejing.spatialarcade.R
import com.kejing.spatialarcade.data.repository.ArcadeCatalog
import com.kejing.spatialarcade.domain.usecase.MusicCourses
import com.kejing.spatialarcade.ui.feature.arcade.*
import com.kejing.spatialarcade.ui.theme.ArcadeColors
import com.pico.spatial.ui.design.*
import com.pico.spatial.ui.design.windows.Sheet
import com.pico.spatial.ui.design.windows.Toolbar
import com.pico.spatial.ui.foundation.content.*
import com.pico.spatial.ui.foundation.gesture.*
import com.pico.spatial.ui.foundation.layout.zOffset
import com.pico.spatial.ui.foundation.layout.requiredDepth
import com.pico.spatial.ui.foundation.layout.Box3D
import com.pico.spatial.ui.foundation.layout.DepthAlignment
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material
import kotlinx.coroutines.CancellationException
import kotlin.math.roundToInt

@Composable private fun Copy(text:String,small:Boolean=false){Text(text,modifier=Modifier.zOffset{3.dp.toPx()},color=if(small)PicoTheme.colorScheme.labelSecondary else PicoTheme.colorScheme.labelPrimary,style=if(small)PicoTheme.typography.bodyMedium else PicoTheme.typography.bodyLarge)}
@Composable private fun Action(text:String,onClick:()->Unit){Button(onClick=onClick,colors=ButtonDefaults.buttonColors(containerColor=PicoTheme.colorScheme.fillPrimary,contentColor=PicoTheme.colorScheme.labelPrimaryLight),modifier=Modifier.height(48.dp)){Text(text,color=PicoTheme.colorScheme.labelPrimaryLight,style=PicoTheme.typography.labelMedium)}}
private fun Modifier.glass()=this.clip(RoundedCornerShape(16.dp)).backgroundMaterial(enable=true,style=Material.Regular)

@Composable fun ArcadeContent(kind:GameKind,state:ArcadeUiState,contacts:List<Contact>,shot:(Float,Int)->Point,scene:ArcadeScene,onEvent:(ArcadeEvent)->Unit){
    val context=LocalContext.current
    Column(Modifier.fillMaxSize().padding(horizontal=24.dp,vertical=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        if(kind==GameKind.B)OrbitHeader(state,onEvent)
        else CompactHeader(kind,state,scene,onEvent)
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()){
          val density=LocalDensity.current
          var panelX by rememberSaveable { mutableFloatStateOf(0f) }
          var panelY by rememberSaveable { mutableFloatStateOf(0f) }
          val minX=with(density){-(maxWidth-264.dp).coerceAtLeast(0.dp).toPx()}
          val limitY=with(density){((maxHeight-408.dp)/2).coerceAtLeast(0.dp).toPx()}
        Row(Modifier.fillMaxSize(),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){
            Box(Modifier.weight(1f).fillMaxHeight()){
                SpatialView(modifier=Modifier.fillMaxSize().pointerInput(scene){
                    // Raw press/release is for the pedal; piston motion uses the SDK's spatial drag stream.
                    if(kind==GameKind.B||kind==GameKind.C){
                        val heldPointers=mutableSetOf<PointerId>()
                        try{detectSpatialPointerEvent(context){events->
                            var handled=false
                            for(info in events){
                                val captured=info.pointerId in heldPointers
                                val pedal=if(kind==GameKind.B)scene.isGravityPedal(info.targetedEntity) else scene.isMusicButton(info.targetedEntity)
                                if(info.isDownEvent()&&pedal)heldPointers.add(info.pointerId)
                                if(!info.pressed||info.isUpEvent())heldPointers.remove(info.pointerId)
                                handled=handled||captured||pedal
                            }
                            if(kind==GameKind.B)scene.pressGravityPedal(heldPointers.isNotEmpty()) else scene.pressMusicButton(heldPointers.isNotEmpty())
                            handled
                        }}finally{if(kind==GameKind.B)scene.pressGravityPedal(false) else scene.pressMusicButton(false)}
                    }
                }.pointerInput(scene){
                    detectSpatialDragGesture(context,targetedToEntity=TargetEntity.any{when(kind){GameKind.B->scene.isOrbitLauncher(it);GameKind.C->it.getName()=="Steel ball / pick and place";GameKind.A->it.getName()=="Boot / heel grab pivot";GameKind.D->it.getName()=="Paddle / captured horizontal drag"}},
                        onDragStart={_,value->scene.grab(value.targetEntity)},
                        onDragEnd={_->scene.release(false)},onDragCancel={_->scene.release(true)}){v->scene.dragPixels(v.dragAmount.x,v.dragAmount.y,v.dragAmount.z)}
                },initial={content,_->try{scene.load(content)}catch(e:CancellationException){throw e}catch(e:Exception){android.util.Log.e("Arcade","Scene load failed",e);scene.vm.error(e.message?:"模型加载失败")}})
                if(!state.ready&&state.error==null)Column(Modifier.align(Alignment.Center)){CircularProgressIndicator();Copy("正在准备模型…")}
                state.error?.let{Copy("模型加载失败：$it")}
            }
            if(kind==GameKind.A)Spacer(Modifier.width(264.dp))
          }
          if(kind==GameKind.A)ContactPanel(state,contacts,onEvent,
              Modifier.align(Alignment.CenterEnd).offset{IntOffset(panelX.roundToInt(),panelY.roundToInt())}){dx,dy->
                  panelX=(panelX+dx).coerceIn(minX,0f);panelY=(panelY+dy).coerceIn(-limitY,limitY)
              }
        }
        if(kind==GameKind.B)Spacer(Modifier.height(12.dp))
    }
    if(state.library&&kind==GameKind.C)LibrarySheet(kind,state,onEvent)
}

/** One compact game HUD combines live title, score, guidance and audio state. */
@Composable private fun OrbitHeader(state:ArcadeUiState,onEvent:(ArcadeEvent)->Unit){
    // Keep all changing glyphs on ONE elevated plane. Per-Text zOffset layers retained
    // old glyphs in the 6.1 emulator when text changed repeatedly (score and both rows).
    Box(Modifier.fillMaxWidth().glass()){
      Row(Modifier.fillMaxWidth().zOffset{3.dp.toPx()}.padding(horizontal=24.dp,vertical=16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(18.dp)){
            Column(Modifier.width(200.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                Text(GameKind.B.title,color=PicoTheme.colorScheme.labelPrimary,style=PicoTheme.typography.titleMedium,maxLines=1,softWrap=false)
                Text("${state.game.score} 分  ·  ${state.game.lives} 球",color=PicoTheme.colorScheme.labelSecondary,style=PicoTheme.typography.bodyMedium,maxLines=1,softWrap=false)
            }
            Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)){
                Box(Modifier.fillMaxWidth().heightIn(min=28.dp).clipToBounds(),contentAlignment=Alignment.Center){
                    Text(state.game.previousStatus.ifEmpty{"准备出发"},modifier=Modifier.fillMaxWidth(),
                        color=PicoTheme.colorScheme.labelSecondary,style=PicoTheme.typography.bodyMedium,
                        textAlign=TextAlign.Center,maxLines=1,softWrap=false,overflow=TextOverflow.Ellipsis)
                }
                Box(Modifier.fillMaxWidth().heightIn(min=32.dp).clipToBounds(),contentAlignment=Alignment.Center){
                    Text(if(state.game.phase==Phase.HELD)"引擎蓄能 ${(state.game.power*100).roundToInt()}% · 松手点火" else state.game.status,
                        modifier=Modifier.fillMaxWidth(),color=PicoTheme.colorScheme.labelPrimary,
                        style=PicoTheme.typography.bodyLarge,textAlign=TextAlign.Center,maxLines=1,softWrap=false,overflow=TextOverflow.Ellipsis)
                }
            }
            Column(Modifier.width(106.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)){
                Text("能量 ${kotlin.math.ceil(state.game.gravityCells.toDouble()).toInt()}/10",color=PicoTheme.colorScheme.labelSecondary,style=PicoTheme.typography.bodyMedium,maxLines=1,softWrap=false)
                Text(when{state.game.gravityActive->"持续引力";state.game.gravityCells<9.999f->"松开充能";else->"缺能 50/格"},
                    color=PicoTheme.colorScheme.labelPrimary,style=PicoTheme.typography.bodyMedium,maxLines=1,softWrap=false)
            }
            ToggleIconButton(checked=state.muted,onCheckedChange={onEvent(ArcadeEvent.ToggleSound)},
                modifier=Modifier.size(48.dp),size=ToggleIconButtonDefaults.toggleIconButtonSize(48.dp),
                colors=ToggleIconButtonDefaults.toggleIconButtonColors(
                    checkedContainerColor=PicoTheme.colorScheme.fillPrimary,checkedContentColor=PicoTheme.colorScheme.labelPrimaryLight,
                    uncheckedContainerColor=PicoTheme.colorScheme.fillTertiary,uncheckedContentColor=PicoTheme.colorScheme.labelPrimary)){
                Icon(painter=painterResource(if(state.muted)R.drawable.ic_sound_off else R.drawable.ic_sound_on),
                    contentDescription=if(state.muted)"取消静音" else "静音",modifier=Modifier.size(24.dp),
                    tint=if(state.muted)PicoTheme.colorScheme.labelPrimaryLight else PicoTheme.colorScheme.labelPrimary)
            }
      }
    }
}

@Composable private fun ContactPanel(state:ArcadeUiState,contacts:List<Contact>,onEvent:(ArcadeEvent)->Unit,modifier:Modifier,onMove:(Float,Float)->Unit){
    val en=state.english
    val context=LocalContext.current
    val movePanel by rememberUpdatedState(onMove)
    val displayedPower=if(state.game.phase==Phase.READY||state.game.phase==Phase.RESULT)0f else state.game.power
    Column(modifier.width(264.dp).glass().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)){
        Row(Modifier.fillMaxWidth().height(44.dp).pointerInput(Unit){detectSpatialDragGesture(context){movePanel(it.dragAmount.x,it.dragAmount.y)}},
            horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
            Text(if(en)"Contact point" else "击球点",style=PicoTheme.typography.titleMedium,color=PicoTheme.colorScheme.labelPrimary)
            Icon(painterResource(R.drawable.ic_grip),if(en)"Drag panel" else "拖动面板",modifier=Modifier.size(24.dp),tint=PicoTheme.colorScheme.labelSecondary)
        }
        Box3D(Modifier.size(220.dp),depthAlignment=DepthAlignment.DepthBack){
          Box(Modifier.size(220.dp).requiredDepth(178.dp),contentAlignment=Alignment.Center){
            SpatialModelView(source=Source.assets("A-selector-ball.glb"),resizability=Resizability.FitInside,modifier=Modifier.size(178.dp).requiredDepth(178.dp)){loading->
                when(loading){is ModelLoadingState.Success->Model(loading.model);is ModelLoadingState.Error->Copy(if(en)"Model unavailable" else "足球加载失败",true);else->CircularProgressIndicator()}
            }
            // One flat hit plane beyond the sphere. All nine targets are reachable by far ray.
            // Native Button owns hover, down/up capture, and cancellation; no depth-varying targets.
            for(p in contacts)Button(onClick={onEvent(ArcadeEvent.Select(p.id))},
                colors=ButtonDefaults.buttonColors(containerColor=Color.Transparent),
                modifier=Modifier.offset((p.u*120).dp,(-p.v*120).dp).size(52.dp).zOffset{184.dp.toPx()}){
                val selected=p.id==state.game.contact
                val marker=if(selected)ArcadeColors.accent(GameKind.A) else PicoTheme.colorScheme.labelPrimary
                Canvas(Modifier.requiredSize(if(selected)22.dp else 14.dp)){
                    drawCircle(Color.White);drawCircle(marker,radius=size.minDimension*.32f)
                }
            }
          }
        }
        // One foreground text plane prevents stale overlapping glyphs on SpatialUI 6.1.
        Column(Modifier.fillMaxWidth().zOffset{6.dp.toPx()},verticalArrangement=Arrangement.spacedBy(10.dp)){
            Text(if(en)CurveCopy.contacts[state.game.contact] else contacts[state.game.contact].label,
                modifier=Modifier.fillMaxWidth().heightIn(min=30.dp),color=PicoTheme.colorScheme.labelPrimary,style=PicoTheme.typography.bodyLarge,
                maxLines=1,overflow=TextOverflow.Ellipsis,textAlign=TextAlign.Center)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                Text(if(en)"Power" else "力度",color=PicoTheme.colorScheme.labelSecondary,style=PicoTheme.typography.bodyMedium)
                Text("${(displayedPower*100).toInt()}%",color=PicoTheme.colorScheme.labelPrimary,style=PicoTheme.typography.bodyMedium)
            }
        }
        val accent=ArcadeColors.accent(GameKind.A)
        Canvas(Modifier.fillMaxWidth().height(6.dp)){
            drawRoundRect(Color.Black.copy(alpha=.12f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(8f))
            drawRoundRect(accent,size=androidx.compose.ui.geometry.Size(size.width*displayedPower,size.height),cornerRadius=androidx.compose.ui.geometry.CornerRadius(8f))
        }
    }
}
@Composable private fun TrajectoryPreview(preset:Int,shot:(Float,Int)->Point){
    val accent=ArcadeColors.accent(GameKind.A)
    Canvas(Modifier.fillMaxWidth().height(88.dp).background(ArcadeColors.Preview,RoundedCornerShape(12.dp))){
        fun at(t:Float):Offset {val p=shot(t,preset);return Offset(size.width*.5f+p.x*size.width*1.5f,size.height*.90f-(.18f-p.z)*size.height*1.4f)}
        for(i in 1..36){val a=shot(i/45f,preset);if(a.y>=.039f&&a.z>=-.27f)drawLine(accent,at((i-1)/45f),at(i/45f),3f)}
        drawCircle(Color.White,6f,at(0f))
    }
}
@Composable private fun LibrarySheet(kind:GameKind,state:ArcadeUiState,onEvent:(ArcadeEvent)->Unit){
    var page by remember { mutableStateOf(state.game.track/4) }
    val pages=(ArcadeCatalog.tracks.size+3)/4
    Sheet(onDismissRequest={onEvent(ArcadeEvent.CloseLibrary)},title={Copy("我的音乐专辑 · ${ArcadeCatalog.tracks.size} 首")}){
        Column(Modifier.width(720.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            Copy("每首歌一条独立轨道。按 ▶ 或放球，跟随音板完整演奏。",true)
            for(track in ArcadeCatalog.tracks.drop(page*4).take(4)){
                Row(Modifier.fillMaxWidth().background(Color.White.copy(alpha=.24f),RoundedCornerShape(16.dp)).padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(18.dp)){
                    val context=LocalContext.current
                    val bitmap=remember(track.id){context.assets.open("album_${track.id.toString().padStart(2,'0')}.png").use{android.graphics.BitmapFactory.decodeStream(it).asImageBitmap()}}
                    Image(bitmap,"${track.title}轨道",Modifier.size(96.dp))
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)){Copy(track.title);Copy(MusicCourses.get(track.id).name+" · "+track.subtitle,true)}
                    Action(if(state.game.track==track.id)"重新装配" else "装配"){onEvent(ArcadeEvent.Track(track.id))}
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                Action("上一页"){page=(page-1).coerceAtLeast(0)};Copy("${page+1} / $pages",true);Action("下一页"){page=(page+1).coerceAtMost(pages-1)}
            }
            Copy("古典曲谱：Mutopia · 音乐盒旋律编曲",true)
        }
    }
}

