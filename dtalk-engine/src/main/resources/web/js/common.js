$('#table').treegrid({
    idField:'id',
    treeField:'name',
    fitColumns:false,
    columns:[[
        {field:'name',title:'菜单',width:'32%'},
        {field:'value',title:'值', width:'40%', formatter : rowFormatter,},
        {field:'comment',title:'描述',width:'30%'},
    ]]
});


// 格式化列的数据
function rowFormatter(value,row){
    if(row.readOnly == false){
        if(row.catalog == 'CMD'){
            var html ='<button type="button" id="b_'+row.id+'" onclick="execute('+row.id+')" class="execute">执行</button>'
        }else{
            var html = '<div id="i_'+row.id+'"><span>'+row.value+'</span><i class="icon_edit right" onclick = "editBtn('+row.id+')"></i></div>'
        }
        return html ;
    }
}


// 查询出所有内容
$.ajax({
    type: "get",
    url: req_prefix + "/dtalk/",
    success: function (data) {
        var list = [];
        if(data.catalog == 'MENU'){
            list = loadMenu(data.childs)
            $('#table').treegrid('loadData', list);
        }
    },
    error: function(XMLHttpRequest) {
        if(XMLHttpRequest.status == 401){
            $('#dialog').dialog('open');
        }
    },
});


// 加载表格数据
var num = 0;    //赋值给id
function loadMenu(data){
    var list = [];
    for(var i=0; i<data.length; i++){
        num++;
        if(data[i].type == 'IP'){
            //转换IP地址
            var array = atob(data[i].value);    //将base转为原来的值
            var arrays = [];
            for(var j= 0; j<array.length; j++){
                arrays.push(array.charCodeAt(j));
            }
            data[i].value = arrays.join('.');
        }else if(data[i].type == 'MAC'){
            //转换物理地址
            var macArray = atob(data[i].value);
            var macArrays = [];
            for(var k= 0; k<macArray.length; k++){
                const paddedHex = ('00' + macArray.charCodeAt(k)).slice(-2);
                macArrays.push(paddedHex)
            }
            data[i].value = macArrays.join(':');
        }else if(data[i].type == 'DATE'){
            data[i].value = tranDate(data[i].value)
        }


        if(!data[i].hide){
            if(data[i].childs.length>0){
                list.push({
                    "id" : num,
                    "name" : (data[i].uiName == null ? data[i].name:data[i].uiName),
                    "value" : data[i].type == "BOOL" && data[i].value == null ? false : data[i].value,
                    "comment" : data[i].description,
                    "children" : data[i].path == "/cmd/time"?[]:loadMenu(data[i].childs,data[i].catalog),
                    "path" : data[i].path,
                    "type" : data[i].type,
                    "catalog" : data[i].catalog,
                    "readOnly" : data[i].readOnly,
                    "regex" : data[i].regex,
                    "state":"closed",
                    "isShow" : false,
                })
            }else{
                if(data[i].uiName!='quit' && data[i].uiName!='back'){
                    var options = [];
                    if(data[i].options){
                        for(var key in data[i].options){
                            options.push({key:key,value:data[i].options[key]})
                        }
                    }
                    // 当类型是SWITCH时，取option值的值为选中的值
                    if(data[i].type == 'SWITCH'){
                        data[i].value = options[data[i].value].value;
                    }
                    // 当类型是MULTICHECK时，取option值的值为选中的值
                    if(data[i].type == 'MULTICHECK'){
                        var number = data[i].value;
                        var checkArr = data[i].value;
                        var newArr = [];
                        for (var j = 0; j < checkArr.length; j++) {
                            newArr.push(options[checkArr[j]].value)
                        }
                        data[i].value = newArr;
                    }


                    list.push({
                        "id" : num,
                        "name" : (data[i].uiName == null ? data[i].name:data[i].uiName),
                        "value" : data[i].type == "BOOL" && data[i].value == null ? false : data[i].value,
                        "comment" : data[i].description,
                        "path" : data[i].path,
                        "type" : data[i].type,
                        "catalog" : data[i].catalog,
                        "readOnly" : data[i].readOnly,
                        "regex" : data[i].regex,
                        "isShow" : false,
                        "options" : options,
                    })
                }
            }
        }
    }
    return list;
}


// 点击修改按钮
var editing;
function editBtn(id){
    var data = $('#table').treegrid('find',id);
    if(editing){    // 判断正在修改中，请先完成修改
        $.messager.alert('温馨提示','请先完成正在修改的内容','info');
    }else{
        $("#i_"+id).hide();
        // 字符串\INTEGER数字\MAC\IP\URL\身份证IDNUM\移动号码MPHONE
        if(data.type == 'STRING' || data.type == 'INTEGER' || data.type == 'MAC' || data.type == 'IP' || data.type == 'URL' || data.type == 'IDNUM' || data.type == 'MPHONE' || data.type == 'EMAIL'){
            var html = '<form class="editForm" id="f_'+data.id+'">'+
                            '<input type="text" placeholder="请输入" value="'+data.value+'">'+
                            '<button type="button" onclick="keep('+id+')" class="keep">保存</button>'+
                            '<button type="button" onclick="cancel('+id+')">取消</button>'+
                        '</form>'
            // 找到修改按钮的父级标签，然后将标签加入
            $("#i_"+id).parent().append(html);
        }


        // date
        if(data.type == 'DATE'){
            var html = '<form class="editForm" id="f_'+data.id+'">'+
                            '<input type="text" placeholder="请输入" id="dateBox" value="'+data.value+'">'+
                            '<button type="button" onclick="keep('+id+')" class="keep">保存</button>'+
                            '<button type="button" onclick="cancel('+id+')">取消</button>'+
                        '</form>'
            // 找到修改按钮的父级标签，然后将标签加入
            $("#i_"+id).parent().append(html);
            // 实例化时间插件
            $('#dateBox').datetimebox({
                showSeconds: true,
            });
        }


        // 密码
        if(data.type == 'PASSWORD'){
            var html = '<form class="editForm" id="f_'+data.id+'">'+
                            '<input type="password" placeholder="请输入" value="'+data.value+'">'+
                            '<button type="button" onclick="keep('+id+')" class="keep">保存</button>'+
                            '<button type="button" onclick="cancel('+id+')">取消</button>'+
                        '</form>'
            // 找到修改按钮的父级标签，然后将标签加入
            $("#i_"+id).parent().append(html);
        }


        // MULTICHECK
        if(data.type == 'MULTICHECK'){
            var html = '<form class="editForm" id="f_'+data.id+'">'+
                            '<input id="checkBox" limitToList="true">'+
                            '<button type="button" onclick="keep('+id+')" class="keep">保存</button>'+
                            '<button type="button" onclick="cancel('+id+')">取消</button>'+
                        '</form>'
            // 找到修改按钮的父级标签，然后将标签加入
            $("#i_"+id).parent().append(html);
            // 实例化多选下拉框
            var opts = [];
            for (var i = 0; i < data.options.length; i++) {
                var haveVal = false;
                var checkArr = data.value;
                for (var j = 0; j < checkArr.length; j++) {
                    if(data.options[i].value == checkArr[j]){
                        haveVal = true;
                    }
                }
                opts.push({
                    "label" : data.options[i].value,
                    "value" : i,
                    "selected" : haveVal
                })
            }
            $('#checkBox').combobox({
                multiple:true,
                valueField: 'value',
                textField: 'label',
                data: opts,
            });
        }


        // switch
        if(data.type == 'SWITCH'){
            var opt = data.options;
            var html = '<form class="editForm" id="f_'+data.id+'">'+
                            '<select id="selected"></select>'+
                            '<button type="button" onclick="keep('+id+')" class="keep">保存</button>'+
                            '<button type="button" onclick="cancel('+id+')">取消</button>'+
                        '</form>'
            // 找到修改按钮的父级标签，然后将标签加入
            $("#i_"+id).parent().append(html);
            for(var i in opt){
                $('#selected').append('<option value="'+i+'">'+opt[i].value+'</option>')
            }
        }


        // BOOL 开关按钮
        if(data.type == 'BOOL'){
            var html = '<form class="editForm" id="f_'+data.id+'">'+
                            '<input id="boolBox" value="'+data.value+'">'+
                            '<button type="button" onclick="keep('+id+')" class="keep">保存</button>'+
                            '<button type="button" onclick="cancel('+id+')">取消</button>'+
                        '</form>'
            // 找到修改按钮的父级标签，然后将标签加入
            $("#i_"+id).parent().append(html);
            // 实例化时间插件
            $('#boolBox').switchbutton({
                disable : true,
                width : "80",
                onText : "true",
                onChange : function(checked){
                    if (checked == true){
                        $("#i_"+id).siblings('.editForm').find('input.switchbutton-value').val(true);
                    }else{
                        $("#i_"+id).siblings('.editForm').find('input.switchbutton-value').val(false);
                    }
                }
            });
            if(data.value){
                $('#boolBox').switchbutton('check')
            }
        }


        // IMAGE
        if(data.type == 'IMAGE'){
             var html = '<form class="editForm" id="f_'+data.id+'">'+
                            '<div class="uploadDiv">'+
                                '<button type="button">上传图片</button>'+
                                '<input type="file" id="file" onchange="previewImage(this,'+id+')"/>'+
                                '<span></span>'+
                            '</div>'+
                            '<button type="button" onclick="keep('+id+')" class="keep">保存</button>'+
                            '<button type="button" onclick="cancel('+id+')">取消</button>'+
                        '</form>'
            // 找到修改按钮的父级标签，然后将标签加入
            $("#i_"+id).parent().append(html);
        }
    }
    editing = true;
};


// 保存按钮
function keep(id){
    var data = $('#table').treegrid('find',id);
    if(data.type == 'SWITCH'){
        var value = $("#f_"+id).find('#selected option:selected').val();
        var text = $("#f_"+id).find('#selected option:selected').text();
    }else if(data.type == 'MULTICHECK'){
        var value = $('#checkBox').combobox('getValues').toString();
        var valueArr = $('#checkBox').combobox('getValues');
        var checkArr = valueArr.map(Number)
        var options = data.options;
        var newArr = [];
        for (var j = 0; j < checkArr.length; j++) {
            newArr.push(options[checkArr[j]].value)
        }
        var text = newArr;
    }else if(data.type == 'DATE'){
        var currentdate = $("#f_"+id).find('input').val();
        var value = new Date(currentdate).getTime();
    }else if(data.type == 'BOOL'){
        var value = $("#f_"+id).find('input.switchbutton-value').val();
    }else if(data.type == 'IMAGE'){
        var value = data.value;
    }else{
        var value = $("#f_"+id).find('input').val();
    }
    if(data.catalog == 'OPTION'){
        var regex = data.regex;
        if(regex){
            if(!value.match(regex)){
                $.messager.alert('温馨提示','请输入正确格式的内容','info');
                return false;
            }
        }


        $.get(req_prefix + '/dtalk'+ data.path + '?value=' + value, function (data,status) {
            if (status = 'success') {
                $("#i_"+id).show().siblings('.editForm').remove();
                // 修改成功之后将修改状态改成已完成
                editing = false;
                $.messager.alert('温馨提示','修改成功','info');
                $('#table').treegrid('reloadFooter')
                if(data.type == 'SWITCH'){
                    $("#i_"+id).find('span').text(text);
                }else if(data.type == 'MULTICHECK'){
                    $("#i_"+id).find('span').text(text)
                }else if(data.type == 'DATE'){
                    var text = tranDate(value);
                    $("#i_"+id).find('span').text(text);
                }else if(data.type == 'IMAGE'){
                    $("#i_"+id).find('span').text('已设置')
                }else{
                    $("#i_"+id).find('span').text(value);
                }
            }
        });
    }
}


// 点击上传图片
function previewImage(file,id) {
    var data = $('#table').treegrid('find',id);
    let reader = new FileReader();//创建读取文件的方法
    let img = event.target.files[0];
    var url = null;
    $(file).siblings('span').text(img.name);
    if(img.size/1024/1024<=1){
        if(img.type.split('/')[1] == "jpeg"||"png"||"jpg"){
            if(window.createObjectURL != undefined) { // basic
                url = window.createObjectURL(img);
            } else if(window.URL != undefined) { // mozilla(firefox)
                url = window.URL.createObjectURL(img);
            } else if(window.webkitURL != undefined) { // webkit or chrome
                url = window.webkitURL.createObjectURL(img);
            }
            reader.readAsDataURL(img)
            reader.onload=function(e){
                var blob = reader.result;
                data.value = blob.substring(blob.indexOf(",") + 1);
            }
        }else{
            $.messager.alert('温馨提示','请上传图片格式的文件','info');
        }
    }else{
        $.messager.alert('温馨提示','图片大于1M，请选择小于1M的图片','info');
    }
}


// 取消按钮
function cancel(id){
    $("#i_"+id).show().siblings('.editForm').remove();
    // 将修改状态改成已完成
    editing = false;
}


// 转化日期格式
function tranDate(date){
    let time = new Date(date)
    let yy = time.getFullYear();
    let MM = time.getMonth() + 1;
    MM = MM < 10 ? ('0' + MM) : MM;
    let dd = time.getDate();
    dd = dd < 10 ? ('0' + dd) : dd;
    let hh = time.getHours();
    hh = hh < 10 ? ('0' + hh) : hh;
    let mm = time.getMinutes();
    mm = mm < 10 ? ('0' + mm) : mm;
    let ss = time.getSeconds();
    ss = ss < 10 ? ('0' + ss) : ss;
    return dd+'/'+MM+'/'+yy+' '+hh+':'+mm+':'+ss;
}


// 表单的聚失焦事件
$('.password').focus(function(){
    $(this).css('border', '1px solid #409eff');
    $(this).siblings('p').hide();
}).blur(function(){
    $(this).css('border', '1px solid #dcdfe6');
});
