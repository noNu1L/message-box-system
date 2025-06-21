#![cfg_attr(
    all(not(debug_assertions), target_os = "windows"),
    windows_subsystem = "windows"
)]

use tauri::menu::{Menu, MenuItem};
use tauri::tray::{MouseButton, TrayIconBuilder, TrayIconEvent};
use tauri::{AppHandle, Manager, PhysicalPosition, Wry};
use tauri_plugin_fs;
use sha2::{Digest, Sha256};

#[cfg(target_os = "windows")]
use windows::Win32::UI::HiDpi::GetDpiForSystem;
#[cfg(target_os = "windows")]
use windows::Win32::UI::WindowsAndMessaging::USER_DEFAULT_SCREEN_DPI;

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_notification::init())
        .setup(move |app| {
            let main_window = app.get_webview_window("main").unwrap();

            // 获取机器码并设置窗口标题
            let machine_id = get_machine_id();
            let title = format!("Message Box Client【{}】", machine_id);
            main_window.set_title(&title).unwrap();

            // 输出系统缩放比例到控制台
            let scale = get_system_scale();
            println!("系统缩放比例: {}", scale);

            // 获取主屏幕分辨率和工作区大小（去除任务栏）
            #[cfg(target_os = "windows")]
            let (work_width, work_height) = {
                use windows::Win32::Foundation::RECT;
                use windows::Win32::Graphics::Gdi::{GetDC, GetDeviceCaps, HORZRES, VERTRES};
                use windows::Win32::UI::WindowsAndMessaging::{
                    SystemParametersInfoW, SPI_GETWORKAREA, SYSTEM_PARAMETERS_INFO_UPDATE_FLAGS,
                };

                let mut work_width = 0;
                let mut work_height = 0;
                unsafe {
                    // 获取屏幕分辨率
                    let hdc = GetDC(None);
                    let width = GetDeviceCaps(hdc, HORZRES);
                    let height = GetDeviceCaps(hdc, VERTRES);
                    println!("屏幕分辨率: {}x{}", width, height);

                    // 获取工作区（去除任务栏）
                    let mut rect = RECT {
                        left: 0,
                        top: 0,
                        right: 0,
                        bottom: 0,
                    };
                    let result = SystemParametersInfoW(
                        SPI_GETWORKAREA,
                        0,
                        Some(&mut rect as *mut _ as _),
                        SYSTEM_PARAMETERS_INFO_UPDATE_FLAGS::default(),
                    );
                    if result.is_ok() {
                        work_width = rect.right - rect.left;
                        work_height = rect.bottom - rect.top;
                        println!("工作区大小（去除任务栏）: {}x{}", work_width, work_height);
                    } else {
                        println!("获取工作区大小失败");
                        work_width = width;
                        work_height = height;
                    }
                }
                (work_width, work_height)
            };
            #[cfg(not(target_os = "windows"))]
            let (work_width, work_height) = { (0, 0) };

            // let window_size = main_window.outer_size()?;
            // let x = work_width - window_size.width as i32;
            // let y = work_height - window_size.height as i32;
            // main_window.set_position(PhysicalPosition { x, y })?;
            // main_window.set_minimizable(false)?;
            // main_window.set_maximizable(false)?;

            // 1. 创建菜单项
            let toggle_item = MenuItem::with_id(app, "toggle", "隐藏", true, None::<&str>)?;
            let quit_item = MenuItem::with_id(app, "quit", "退出", true, None::<&str>)?;

            // 2. 克隆句柄，用于在不同的事件监听器中操作
            let toggle_handle_for_menu = toggle_item.clone();
            let toggle_handle_for_tray_click = toggle_item.clone();
            let toggle_handle_for_close = toggle_item.clone();
            let window_for_close = main_window.clone();

            let menu = Menu::with_items(app, &[&toggle_item, &quit_item])?;

            let _tray = TrayIconBuilder::new()
                .icon(app.default_window_icon().unwrap().clone())
                .tooltip("Message Box Client")
                .menu(&menu)
                .on_menu_event(move |app, event| {
                    if event.id.as_ref() == "toggle" {
                        toggle_window_visibility(app, &toggle_handle_for_menu);
                    } else if event.id.as_ref() == "quit" {
                        app.exit(0);
                    }
                })
                .on_tray_icon_event(move |tray, event| {
                    if let TrayIconEvent::Click { button, .. } = event {
                        if button == MouseButton::Right {
                            // 这里可以自定义弹窗（如调用 JS 显示自定义菜单）
                            //                               let app = tray.app_handle();
                            //                               toggle_window_visibility(app, &toggle_handle_for_tray_click);
                            // .menu(&menu)
                        }
                        // 左键什么都不做
                    }
                })
                .build(app)?;

            // 3. 拦截窗口关闭事件
            main_window.on_window_event(move |event| {
                if let tauri::WindowEvent::CloseRequested { api, .. } = event {
                    api.prevent_close();
                    window_for_close.hide().unwrap();
                    toggle_handle_for_close.set_text("显示").unwrap();
                }
            });

            main_window.show()?;
            main_window.set_focus()?;
            Ok(())
        })
        .invoke_handler(tauri::generate_handler![get_system_scale, get_machine_id])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}

// 注意这里的泛型参数 <Wry>
fn toggle_window_visibility(app: &AppHandle, toggle_item: &MenuItem<Wry>) {
    if let Some(window) = app.get_webview_window("main") {
        if let Ok(true) = window.is_visible() {
            window.hide().unwrap();
            toggle_item.set_text("显示").unwrap();
        } else {
            window.show().unwrap();
            window.set_focus().unwrap();
            toggle_item.set_text("隐藏").unwrap();
        }
    }
}

#[tauri::command]
fn get_system_scale() -> f64 {
    #[cfg(target_os = "windows")]
    {
        unsafe {
            let dpi = GetDpiForSystem();
            dpi as f64 / USER_DEFAULT_SCREEN_DPI as f64
        }
    }
    #[cfg(not(target_os = "windows"))]
    {
        1.0
    }
}

#[tauri::command]
fn get_machine_id() -> String {
    let original_id = machine_uid::get().unwrap_or_else(|_| "unknown".to_string());
    let mut hasher = Sha256::new();
    hasher.update(original_id.as_bytes());
    let result = hasher.finalize();
    let full_hash = format!("{:x}", result);
    // 截断为前8个字符以缩短ID，同时保持较低的碰撞概率
    full_hash[..8].to_string()
}
