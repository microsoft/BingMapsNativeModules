function formatCode
{
find $1 -name "*.[hm]" -print | xargs clang-format -i
find $1 -name "*.[hm]" -print | xargs sed -i "" -E 's/([\*\^])_Nullable/\1 _Nullable/g'
find $1 -name "*.[hm]" -print | xargs sed -i "" -E 's/([\*\^])_Nonnull/\1 _Nonnull/g'
find $1 -name "*.[hm]" -print | xargs sed -i "" 's/*__autoreleasing/* __autoreleasing/g'
}

if which clang-format > /dev/null; then
formatCode MSMapsModules
formatCode MSMapsModulesTests
else
echo 'Unable to find clang-format! You can install it with "brew install clang-format"'
exit 1
fi