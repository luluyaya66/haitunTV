<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>海豚TV (HaitunTV)</title>
    <link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        .feature-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
        }
        .tech-item:hover {
            background-color: #f0fdf4;
        }
        .version-item:hover {
            background-color: #f0f9ff;
        }
    </style>
</head>
<body class="bg-gradient-to-br from-blue-50 to-indigo-100 min-h-screen">
    <div class="container mx-auto px-4 py-8 max-w-6xl">
        <!-- 头部标题 -->
        <header class="text-center mb-12">
            <h1 class="text-4xl md:text-5xl font-bold text-indigo-800 mb-4">海豚TV (HaitunTV)</h1>
            <p class="text-xl text-gray-600 max-w-3xl mx-auto">
                一款功能强大的Android TV直播应用，支持多种电视频道观看，提供丰富的节目指南和个性化设置。
            </p>
        </header>

        <!-- 双列布局主内容 -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <!-- 左侧内容 -->
            <div class="space-y-8">
                <!-- 功能特点 -->
                <section class="bg-white rounded-2xl shadow-lg p-6">
                    <h2 class="text-2xl font-bold text-gray-800 mb-6 flex items-center">
                        <i class="fas fa-star text-yellow-500 mr-3"></i>功能特点
                    </h2>
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <!-- 直播功能 -->
                        <div class="feature-card bg-gradient-to-br from-blue-50 to-cyan-50 rounded-xl p-5 border border-blue-100 transition-all duration-300">
                            <h3 class="text-lg font-semibold text-blue-700 mb-3 flex items-center">
                                <i class="fas fa-tv mr-2"></i>直播功能
                            </h3>
                            <ul class="space-y-2 text-gray-600">
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>支持海量电视频道直播</span>
                                </li>
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>高清流畅的播放体验</span>
                                </li>
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>多种播放源选择</span>
                                </li>
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>快速频道切换</span>
                                </li>
                            </ul>
                        </div>

                        <!-- 频道管理 -->
                        <div class="feature-card bg-gradient-to-br from-purple-50 to-fuchsia-50 rounded-xl p-5 border border-purple-100 transition-all duration-300">
                            <h3 class="text-lg font-semibold text-purple-700 mb-3 flex items-center">
                                <i class="fas fa-th-large mr-2"></i>频道管理
                            </h3>
                            <ul class="space-y-2 text-gray-600">
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>频道分组分类</span>
                                </li>
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>频道收藏功能</span>
                                </li>
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>自定义频道排序</span>
                                </li>
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>频道搜索功能</span>
                                </li>
                            </ul>
                        </div>

                        <!-- 节目指南 -->
                        <div class="feature-card bg-gradient-to-br from-amber-50 to-orange-50 rounded-xl p-5 border border-amber-100 transition-all duration-300">
                            <h3 class="text-lg font-semibold text-amber-700 mb-3 flex items-center">
                                <i class="fas fa-calendar-alt mr-2"></i>节目指南
                            </h3>
                            <ul class="space-y-2 text-gray-600">
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>实时节目信息显示</span>
                                </li>
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>详细的节目预告</span>
                                </li>
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>节目时间表</span>
                                </li>
                            </ul>
                        </div>

                        <!-- 个性化设置 -->
                        <div class="feature-card bg-gradient-to-br from-emerald-50 to-teal-50 rounded-xl p-5 border border-emerald-100 transition-all duration-300">
                            <h3 class="text-lg font-semibold text-emerald-700 mb-3 flex items-center">
                                <i class="fas fa-cog mr-2"></i>个性化设置
                            </h3>
                            <ul class="space-y-2 text-gray-600">
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>解码器选择</span>
                                </li>
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>网络设置优化</span>
                                </li>
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>界面个性化</span>
                                </li>
                                <li class="flex items-start">
                                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 text-sm"></i>
                                    <span>开机启动选项</span>
                                </li>
                            </ul>
                        </div>
                    </div>
                </section>

                <!-- 技术架构 -->
                <section class="bg-white rounded-2xl shadow-lg p-6">
                    <h2 class="text-2xl font-bold text-gray-800 mb-6 flex items-center">
                        <i class="fas fa-code text-indigo-500 mr-3"></i>技术架构
                    </h2>
                    <div class="mb-6">
                        <h3 class="text-lg font-semibold text-gray-700 mb-3">核心技术栈</h3>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                            <div class="tech-item bg-blue-50 rounded-lg p-4 border border-blue-100">
                                <div class="font-medium text-blue-800">开发语言</div>
                                <div class="text-gray-600">Java</div>
                            </div>
                            <div class="tech-item bg-green-50 rounded-lg p-4 border border-green-100">
                                <div class="font-medium text-green-800">播放引擎</div>
                                <div class="text-gray-600">IjkPlayer</div>
                            </div>
                            <div class="tech-item bg-purple-50 rounded-lg p-4 border border-purple-100">
                                <div class="font-medium text-purple-800">图片加载</div>
                                <div class="text-gray-600">Glide</div>
                            </div>
                            <div class="tech-item bg-amber-50 rounded-lg p-4 border border-amber-100">
                                <div class="font-medium text-amber-800">网络请求</div>
                                <div class="text-gray-600">OkHttp</div>
                            </div>
                            <div class="tech-item bg-cyan-50 rounded-lg p-4 border border-cyan-100 md:col-span-2">
                                <div class="font-medium text-cyan-800">架构模式</div>
                                <div class="text-gray-600">MVVM + Controller</div>
                            </div>
                        </div>
                    </div>
                </section>
            </div>

            <!-- 右侧内容 -->
            <div class="space-y-8">
                <!-- 语音控制 -->
                <section class="bg-white rounded-2xl shadow-lg p-6">
                    <h2 class="text-2xl font-bold text-gray-800 mb-6 flex items-center">
                        <i class="fas fa-microphone-alt text-red-500 mr-3"></i>语音控制
                    </h2>
                    <div class="feature-card bg-gradient-to-br from-rose-50 to-pink-50 rounded-xl p-5 border border-rose-100">
                        <ul class="space-y-3 text-gray-600">
                            <li class="flex items-start">
                                <i class="fas fa-microphone text-red-500 mt-1 mr-3"></i>
                                <span>支持夏杰语音助手</span>
                            </li>
                            <li class="flex items-start">
                                <i class="fas fa-exchange-alt text-red-500 mt-1 mr-3"></i>
                                <span>语音频道切换</span>
                            </li>
                            <li class="flex items-start">
                                <i class="fas fa-search text-red-500 mt-1 mr-3"></i>
                                <span>语音节目搜索</span>
                            </li>
                        </ul>
                    </div>
                </section>

                <!-- 网络优化 -->
                <section class="bg-white rounded-2xl shadow-lg p-6">
                    <h2 class="text-2xl font-bold text-gray-800 mb-6 flex items-center">
                        <i class="fas fa-network-wired text-green-500 mr-3"></i>网络优化
                    </h2>
                    <div class="feature-card bg-gradient-to-br from-lime-50 to-green-50 rounded-xl p-5 border border-lime-100">
                        <ul class="space-y-3 text-gray-600">
                            <li class="flex items-start">
                                <i class="fas fa-signal text-green-500 mt-1 mr-3"></i>
                                <span>网络状态监控</span>
                            </li>
                            <li class="flex items-start">
                                <i class="fas fa-sync-alt text-green-500 mt-1 mr-3"></i>
                                <span>自动网络重连</span>
                            </li>
                            <li class="flex items-start">
                                <i class="fas fa-tachometer-alt text-green-500 mt-1 mr-3"></i>
                                <span>网络速度显示</span>
                            </li>
                        </ul>
                    </div>
                </section>

                <!-- 系统要求 -->
                <section class="bg-white rounded-2xl shadow-lg p-6">
                    <h2 class="text-2xl font-bold text-gray-800 mb-6 flex items-center">
                        <i class="fas fa-laptop-code text-purple-500 mr-3"></i>系统要求
                    </h2>
                    <div class="space-y-4">
                        <div class="flex items-start p-4 bg-blue-50 rounded-lg border border-blue-100">
                            <i class="fas fa-android text-green-600 text-xl mt-1 mr-3"></i>
                            <div>
                                <div class="font-medium text-gray-800">Android版本</div>
                                <div class="text-gray-600">Android 4.4 (API 19) 及以上</div>
                            </div>
                        </div>
                        <div class="flex items-start p-4 bg-indigo-50 rounded-lg border border-indigo-100">
                            <i class="fas fa-tv text-indigo-600 text-xl mt-1 mr-3"></i>
                            <div>
                                <div class="font-medium text-gray-800">设备要求</div>
                                <div class="text-gray-600">Android TV设备或支持大屏模式的Android设备</div>
                            </div>
                        </div>
                        <div class="flex items-start p-4 bg-cyan-50 rounded-lg border border-cyan-100">
                            <i class="fas fa-wifi text-cyan-600 text-xl mt-1 mr-3"></i>
                            <div>
                                <div class="font-medium text-gray-800">网络要求</div>
                                <div class="text-gray-600">稳定的网络连接</div>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- 版本历史 -->
                <section class="bg-white rounded-2xl shadow-lg p-6">
                    <h2 class="text-2xl font-bold text-gray-800 mb-6 flex items-center">
                        <i class="fas fa-history text-amber-500 mr-3"></i>版本历史
                    </h2>
                    <div class="space-y-4">
                        <div class="version-item bg-gradient-to-r from-blue-50 to-cyan-50 rounded-lg p-4 border border-blue-100">
                            <div class="flex justify-between items-center">
                                <span class="font-bold text-blue-700">1.3.2</span>
                                <span class="text-sm text-gray-500">最新版本</span>
                            </div>
                            <div class="mt-2 text-gray-600">优化播放体验</div>
                        </div>
                        <div class="version-item bg-gradient-to-r from-purple-50 to-fuchsia-50 rounded-lg p-4 border border-purple-100">
                            <div class="font-bold text-purple-700">1.3.0</div>
                            <div class="mt-2 text-gray-600">重构架构，提升性能</div>
                        </div>
                        <div class="version-item bg-gradient-to-r from-amber-50 to-orange-50 rounded-lg p-4 border border-amber-100">
                            <div class="font-bold text-amber-700">1.2.0</div>
                            <div class="mt-2 text-gray-600">添加语音控制功能</div>
                        </div>
                        <div class="version-item bg-gradient-to-r from-emerald-50 to-teal-50 rounded-lg p-4 border border-emerald-100">
                            <div class="font-bold text-emerald-700">1.1.0</div>
                            <div class="mt-2 text-gray-600">初始版本</div>
                        </div>
                    </div>
                </section>

                <!-- 注意事项 -->
                <section class="bg-white rounded-2xl shadow-lg p-6">
                    <h2 class="text-2xl font-bold text-gray-800 mb-6 flex items-center">
                        <i class="fas fa-exclamation-triangle text-yellow-500 mr-3"></i>注意事项
                    </h2>
                    <div class="space-y-3 text-gray-600">
                        <div class="flex items-start">
                            <i class="fas fa-circle text-yellow-400 text-xs mt-2 mr-3"></i>
                            <span>本应用仅用于个人学习和研究目的</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-circle text-yellow-400 text-xs mt-2 mr-3"></i>
                            <span>所有频道内容均来自网络，版权归原作者所有</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-circle text-yellow-400 text-xs mt-2 mr-3"></i>
                            <span>请确保在合法合规的情况下使用本应用</span>
                        </div>
                       
