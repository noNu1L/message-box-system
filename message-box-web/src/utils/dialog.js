import {ElMessageBox} from 'element-plus'


const alter = (title, message, okCallback, cancelCallback) => {
    ElMessageBox.alert(message, title, {
        // if you want to disable its autofocus
        // autofocus: false,
        confirmButtonText: '确认',
        callback: (action) => {

            if (action === 'confirm') {
                okCallback && okCallback();
            }
            if (action === 'cancel') {
                cancelCallback && cancelCallback();
            }
            // if ( === 'confirm') {
            // // ${action}
            // ElMessage({
            //     type: 'info',
            //     message: `action: ${action}`,
            // })
        },
    })
}

export default {
    alter
}