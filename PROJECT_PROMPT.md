مشروع: بناء عميل Telegram متطور جدًا بواجهة أصلية واحترافية

أريد منك بناء تطبيق مراسلة Android احترافي جدًا يعتمد على Telegram API + TDLib، ويكون عميل Telegram مخصصًا عالي الجودة، وليس مجرد واجهة تجريبية أو Mockup.

القاعدة الأساسية

لا تخترع نظام مراسلة وهميًا ولا تستخدم بيانات تجريبية في النسخة النهائية.

استخدم:

- Telegram API
- TDLib
- API ID الخاص بالتطبيق
- API Hash الخاص بالتطبيق
- Android Native architecture
- مشروع مفتوح المصدر مناسب ومتوافق مع التراخيص عند الحاجة

TDLib يجب أن يكون مسؤولًا عن:

- الاتصال
- المصادقة
- إدارة الجلسات
- التحديثات
- التخزين المحلي
- التشفير
- الرسائل
- الوسائط
- المجموعات
- القنوات
- البحث
- الملفات
- الإشعارات
- التعامل مع حالات انقطاع الإنترنت

لا تبنِ طبقة بديلة لهذه الوظائف إلا عندما يكون ذلك ضروريًا لميزة أصلية خاصة بالتطبيق.

---

المرحلة 1 — تحليل المشروع

قبل كتابة الكود:

1. افحص المشروع بالكامل.
2. افحص جميع الملفات والمجلدات.
3. حدد لغة المشروع وإصدارات Android وGradle وKotlin.
4. حدد المكتبات الحالية.
5. اكتشف أي كود تجريبي أو Mock data.
6. اكتشف أي أخطاء معمارية.
7. حدد طريقة دمج TDLib الأنسب.
8. أنشئ خطة تنفيذ مرحلية.
9. لا تبدأ بتغيير الواجهة قبل فهم البنية الحالية.

بعد التحليل ابدأ التنفيذ تلقائيًا.

---

المرحلة 2 — Telegram API وTDLib

أنشئ نظام إعدادات آمن لـ:

TELEGRAM_API_ID
TELEGRAM_API_HASH

لا تضع الأسرار داخل GitHub.

اجعل الإعدادات قابلة للتهيئة عبر:

- local.properties
- Gradle properties
- environment variables

ولا تضع API Hash في الواجهة أو Logs.

ادمج TDLib بطريقة صحيحة ومستقرة.

تعامل مع:

- authorizationState
- phone number
- verification code
- 2FA password
- logout
- session persistence
- network changes
- reconnect
- database encryption key

لا تحفظ كلمات المرور أو رموز تسجيل الدخول في Logs.

---

المرحلة 3 — تسجيل الدخول

أنشئ تجربة تسجيل دخول احترافية:

1. اختيار الدولة.
2. إدخال رقم الهاتف.
3. إرسال رمز Telegram.
4. إدخال الرمز.
5. دعم 2FA.
6. معالجة الأخطاء.
7. إعادة إرسال الرمز عند السماح.
8. تسجيل الخروج.
9. الاحتفاظ بالجلسة بشكل آمن.
10. دعم تسجيل الدخول عبر QR إذا كان مدعومًا في TDLib/API.

اجعل شاشة الدخول سريعة وهادئة ومناسبة للموبايل.

---

المرحلة 4 — الواجهة الرئيسية

صمم واجهة حديثة جدًا مستوحاة من تطبيقات المراسلة الحديثة، لكن لا تنسخ واجهة Telegram الرسمية حرفيًا.

الواجهة يجب أن تكون:

- سريعة جدًا
- نظيفة
- ناعمة
- Responsive
- Material 3
- دعم Dark Mode
- دعم Light Mode
- دعم العربية RTL
- دعم الإنجليزية
- Animations خفيفة
- انتقالات سلسة
- Skeleton loading
- Empty states
- Error states

الشاشة الرئيسية تحتوي على:

- قائمة المحادثات
- البحث
- زر إنشاء محادثة
- الرسائل المثبتة
- المجلدات
- القنوات
- المجموعات
- جهات الاتصال
- Saved Messages
- إعدادات
- الحساب الشخصي

---

المرحلة 5 — نظام المحادثات

نفذ جميع الوظائف الأساسية الحقيقية:

- إرسال رسالة
- استقبال رسالة
- تعديل الرسالة
- حذف الرسالة
- حذف للجميع عند السماح
- Reply
- Forward
- Copy
- Pin
- Unpin
- React
- Mention
- Hashtag
- Links
- Rich text
- Markdown-like formatting عند دعم API
- البحث داخل المحادثة
- الرسائل المثبتة
- الرسائل المحفوظة
- Scheduled messages إذا كان API يدعمها
- Silent messages
- إرسال الوسائط
- إرسال الملفات
- إرسال الموقع
- إرسال جهات الاتصال

---

المرحلة 6 — تجربة الكتابة

اجعل Composer احترافيًا جدًا.

يجب أن يدعم:

- نص
- Emoji
- Stickers
- GIF
- صور
- فيديو
- ملفات
- Voice messages
- Video notes
- Reply preview
- Edit mode
- Forward mode
- Multiple attachments
- Drag/drop حيثما كان مدعومًا
- Preview للصور والفيديو
- Progress indicator
- Upload cancellation
- Retry failed uploads

اجعل زر الإرسال يتغير ديناميكيًا حسب الحالة.

---

المرحلة 7 — الوسائط

أنشئ Media Viewer قويًا:

- صور Fullscreen
- Zoom
- Swipe
- فيديو
- Picture-in-picture عند دعم النظام
- تحميل
- مشاركة
- حفظ
- معلومات الملف
- Progress
- Retry
- فتح الملفات الخارجية عند الحاجة

استخدم caching ذكيًا.

لا تعيد تحميل الوسائط الموجودة محليًا.

---

المرحلة 8 — الصوت والفيديو

نفذ:

- Voice messages
- Recording UI احترافي
- Waveform
- Pause/Resume
- Cancel
- Playback speed
- Seek
- Video playback
- Video notes
- Picture-in-picture عند الإمكان

استخدم ExoPlayer/Media3 إذا كان مناسبًا للمشروع.

---

المرحلة 9 — البحث الذكي

أنشئ بحثًا سريعًا يدعم:

- المحادثات
- الأشخاص
- الرسائل
- الملفات
- الصور
- الفيديو
- الروابط
- Saved Messages

وأضف فلاتر بحث جميلة.

اجعل البحث سريعًا حتى مع آلاف المحادثات.

---

المرحلة 10 — المجلدات والتنظيم

أنشئ نظام تنظيم متطور:

- All
- Unread
- Personal
- Groups
- Channels
- Bots
- Custom folders

اسمح للمستخدم بإنشاء مجلدات مخصصة وترتيبها.

---

المرحلة 11 — الملف الشخصي

صمم Profile حديثًا جدًا يحتوي على:

- الصورة
- الاسم
- username
- bio
- shared media
- shared files
- shared links
- notifications
- mute
- search
- block/report عند توفرها
- call/video call إذا كان API/TDLib يدعمها

---

المرحلة 12 — Premium

مهم جدًا:

لا تتجاوز اشتراك Telegram Premium.

لا تحاول تزوير حالة Premium.

لا تتلاعب بالـAPI أو limits.

بدل ذلك:

1. اكتشف حالة الحساب الحقيقية.
2. إذا كان الحساب Premium، فعّل ميزات Premium التي يدعمها API.
3. إذا لم يكن Premium، اعرض تجربة Premium الرسمية والاشتراك عند توفره.
4. لا تستخدم أي exploit أو bypass.
5. لا تجعل التطبيق يدّعي أن المستخدم Premium وهو ليس كذلك.

نفذ دعمًا ممتازًا للميزات التي يتيحها Telegram API/TDLib، مثل:

- Premium stickers
- custom emoji
- animated profile pictures
- emoji status
- profile colors عند توفرها
- additional reactions
- voice transcription عند توفرها
- translation عند توفرها
- faster downloads وفق حالة الحساب
- larger uploads وفق الحدود الفعلية
- Business features وفق حالة الحساب
- AI features إذا كانت متاحة رسميًا
- Premium badge

اقرأ إعدادات Telegram الحالية من API بدل hard-coding للحدود، لأن الحدود والميزات قد تتغير.

---

المرحلة 13 — ميزات أصلية إضافية

أريد أن يكون التطبيق أكثر من مجرد Clone.

أضف ميزات أصلية لا تتعارض مع Telegram API:

Smart Chat Tools

- أدوات تنظيم المحادثة
- تذكيرات محلية
- تصنيف محلي للمحادثات
- Quick actions
- أدوات بحث متقدمة

Smart Media

- مدير وسائط
- تصفية الملفات الكبيرة
- أدوات التخزين المؤقت
- تنظيف Cache ذكي
- إحصائيات التخزين

Customization

- Themes
- Accent colors
- Chat backgrounds
- Chat bubbles
- Font size
- Message density
- Animation controls
- App icons إذا كان مسموحًا

Productivity

- Quick replies
- Saved templates
- Message drafts
- Scheduled local reminders
- Favorites
- Smart folders

---

المرحلة 14 — الأداء

الأداء أولوية قصوى.

يجب أن يعمل التطبيق بسلاسة مع:

- 100 محادثة
- 1,000 محادثة
- 10,000+ رسالة

حسّن:

- RecyclerView/Lazy lists
- Image loading
- Memory
- Database access
- Coroutines
- background tasks
- caching
- pagination
- recomposition إن كان Jetpack Compose مستخدمًا

لا تحمل آلاف الرسائل دفعة واحدة.

استخدم pagination وlazy loading.

---

المرحلة 15 — العمل بدون إنترنت

يجب أن تكون تجربة Offline ممتازة.

عند انقطاع الإنترنت:

- اعرض البيانات المخزنة محليًا.
- أظهر حالة Offline بوضوح.
- احتفظ بالرسائل المعلقة.
- أعد المحاولة تلقائيًا عند عودة الاتصال.
- لا تتسبب في duplicate messages.
- لا تفقد drafts.

---

المرحلة 16 — الإشعارات

نفذ إشعارات احترافية:

- رسائل جديدة
- مجموعات
- قنوات
- Replies
- Mentions
- Media notifications
- grouped notifications
- notification actions
- Mark as read
- Reply

احترم إعدادات Telegram الفعلية.

---

المرحلة 17 — الأمان

راجع التطبيق بالكامل أمنيًا.

ممنوع:

- تسجيل كلمات المرور
- تسجيل OTP
- تسريب API Hash
- تخزين أسرار في Git
- HTTP غير الآمن عندما لا يكون مطلوبًا
- Debug logging في Release

استخدم:

- encrypted local storage
- secure configuration
- Android Keystore حيث يلزم
- certificate/network security configuration المناسبة
- ProGuard/R8 في Release

---

المرحلة 18 — معالجة الأخطاء

كل RPC/API error يجب أن يكون له تعامل واضح.

لا تعرض للمستخدم:

"Exception"

أو:

"Unknown error"

بل اعرض رسالة مفهومة.

أضف:

- Retry
- Cancel
- reconnect
- offline state
- timeout handling
- rate-limit handling
- flood wait handling

ولا تحاول تجاوز Flood limits.

---

المرحلة 19 — التصميم

أريد Design System كامل:

Typography
Spacing
Shapes
Icons
Colors
Elevation
Animations
Loading
Dialogs
Bottom sheets
Snackbars
Empty states

التصميم يجب أن يبدو كمنتج حقيقي من شركة تقنية كبيرة، وليس مشروعًا مولدًا آليًا.

لا تستخدم:

- ألوان مبالغًا فيها
- gradients عشوائية
- أزرار ضخمة
- Shadows مبالغ فيها
- animations مزعجة

---

المرحلة 20 — العربية

العربية أولوية.

أضف RTL حقيقيًا لكل التطبيق.

اختبر:

- المحادثات
- أسماء المستخدمين
- النصوص المختلطة عربي/إنجليزي
- الأرقام
- التواريخ
- الروابط
- الرسائل الطويلة
- Emoji

ولا تستخدم حلول RTL سطحية.

---

المرحلة 21 — الاختبارات

أنشئ:

- Unit tests
- UI tests
- Integration tests

اختبر على الأقل:

Login
Logout
Messaging
Media
Files
Search
Folders
Notifications
Offline
Reconnect
Dark mode
RTL
Large chats
Large files
Low memory
Slow network

---

المرحلة 22 — إصلاح ذاتي

بعد كل مرحلة:

1. Build.
2. Run tests.
3. Analyze errors.
4. Fix errors.
5. Build again.
6. Run tests again.
7. لا تنتقل للمرحلة التالية حتى تصبح المرحلة الحالية مستقرة.

إذا وجدت خطأ أثناء التطوير، لا تتجاهله ولا تضع TODO بدل إصلاحه.

---

المرحلة 23 — مراجعة نهائية

في النهاية افحص المشروع كاملًا بحثًا عن:

- crashes
- memory leaks
- race conditions
- UI bugs
- broken navigation
- broken back button
- broken RTL
- broken dark mode
- API errors
- authentication problems
- duplicate messages
- media failures
- notification failures
- performance issues
- security issues

ثم أصلح كل شيء.

---

قواعد صارمة

1. لا تستخدم Mock data في الوظائف النهائية.
2. لا تدّعي أن ميزة تعمل قبل اختبارها.
3. لا تترك أزرارًا وهمية.
4. لا تترك TODOs للوظائف الأساسية.
5. لا تكسر الميزات الموجودة أثناء إضافة الميزات الجديدة.
6. لا تضع API Hash داخل Git.
7. لا تتجاوز Telegram Premium أو حدود Telegram.
8. لا تتجاوز Flood limits.
9. لا تنتحل اسم Telegram أو شعاره.
10. التزم بترخيص أي مشروع مفتوح المصدر تستخدمه.
11. استخدم API ID الخاص بالتطبيق.
12. اجعل الكود Modular وقابلًا للصيانة.
13. اجعل جميع النصوص قابلة للترجمة.
14. اجعل التطبيق يعمل جيدًا على الأجهزة المتوسطة والضعيفة.
15. لا تنتقل للمرحلة التالية قبل نجاح الاختبارات.

النتيجة المطلوبة

أريد في النهاية تطبيق Android حقيقي ومستقر، وليس Prototype.

يجب أن يشعر المستخدم أنه أمام تطبيق مراسلة Premium حديث جدًا، سريع، جميل، متكامل، ويستخدم Telegram API/TDLib فعليًا.

ابدأ أولًا بتحليل المشروع الحالي، ثم نفذ المراحل واحدة تلو الأخرى، واختبر وأصلح بعد كل مرحلة.

---

## المرحلة الأخيرة — GitHub والمستودع والتحقق

بعد الانتهاء من البناء والاختبار وإصلاح الأخطاء، أنشئ مستودع GitHub عامًا باسم احترافي وفريد، وارفع جميع ملفات المشروع الضرورية للبناء والتطوير، وأنشئ README احترافيًا يشرح المشروع والمميزات والتقنيات والتشغيل وإعداد Telegram API ID وAPI Hash وبناء Release والمتطلبات والتراخيص ومصادر المشاريع مفتوحة المصدر المستخدمة.

ممنوع رفع API Hash أو كلمات المرور أو OTP أو مفاتيح الخدمات أو Tokens أو Keystore أو ملفات secrets أو أي بيانات اعتماد شخصية. استخدم placeholders وlocal.properties أو متغيرات البيئة، وأضف الملفات الحساسة إلى .gitignore.

بعد الرفع، تحقق من أن المستودع Public، وافتحه للتأكد من وجود الملفات ووضوح README، وتحقق من قابلية الاستنساخ والبناء، ونفذ فحصًا مناسبًا للأسرار، ثم أنشئ Commit نهائيًا واضحًا. يجب تسليم الرابط الكامل المباشر بصيغة https://github.com/USERNAME/REPOSITORY.
