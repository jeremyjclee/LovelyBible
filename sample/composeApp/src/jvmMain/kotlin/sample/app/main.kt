import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.lovelybible.di.allModules
import com.lovelybible.di.jvmModule
import com.lovelybible.feature.main.MainScreen
import com.lovelybible.feature.navigation.NavigationViewModel
import com.lovelybible.feature.presentation.PresentationIntent
import com.lovelybible.feature.presentation.PresentationViewModel
import com.lovelybible.feature.presentation.PresentationWindow
import com.lovelybible.theme.LovelyBibleTheme
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import java.awt.Dimension

fun main() {
    // Koin DI 초기화 - application 블록 외부에서 한 번만 실행
    try {
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                modules(jvmModule + allModules)
            }
        }
    } catch (e: Exception) {
        // DI 초기화 실패 시 에러 대화상자 표시
        javax.swing.JOptionPane.showMessageDialog(
            null,
            "DI 초기화 실패: ${e.message}\n${e.stackTraceToString().take(1000)}",
            "LovelyBible Error",
            javax.swing.JOptionPane.ERROR_MESSAGE
        )
        return
    }
    
    // ViewModels 주입 - application 블록 외부에서 한 번만 실행
    val navigationViewModel: NavigationViewModel
    val presentationViewModel: PresentationViewModel
    try {
        navigationViewModel = GlobalContext.get().get()
        presentationViewModel = GlobalContext.get().get()
    } catch (e: Exception) {
        // ViewModel 주입 실패 시 에러 대화상자 표시
        javax.swing.JOptionPane.showMessageDialog(
            null,
            "ViewModel 주입 실패: ${e.message}\n${e.stackTraceToString().take(1000)}",
            "LovelyBible Error",
            javax.swing.JOptionPane.ERROR_MESSAGE
        )
        return
    }
    
    application {
        val icon = painterResource("icon.png")
        
        // 메인 윈도우
        Window(
            title = "Lovely Bible",
            icon = icon,
            state = rememberWindowState(width = 1280.dp, height = 800.dp),
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(1024, 600)
            
            // NavigationViewModel 상태 변경 시 PresentationViewModel 동기화
            LaunchedEffect(navigationViewModel.state.displayedVerses, navigationViewModel.state.currentTitle) {
                presentationViewModel.updateFromNavigation(
                    navigationViewModel.state.displayedVerses,
                    navigationViewModel.state.currentTitle
                )
            }
            
            LovelyBibleTheme {
                MainScreen()
            }
        }
        
        // 프레젠테이션 윈도우
        PresentationWindow(
            state = presentationViewModel.state,
            onClose = { presentationViewModel.onIntent(PresentationIntent.ClosePresentation) },
            onAction = presentationViewModel::onIntent,
            icon = icon
        )
    }
}