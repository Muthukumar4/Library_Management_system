 = Get-Content  lib m/admin.html
for ( = 0;  -lt .Length; ++) {
  if ([] -match Users Directory) {
    Write-Output ( + 1).ToString() + :  + []
    break
  }
}
